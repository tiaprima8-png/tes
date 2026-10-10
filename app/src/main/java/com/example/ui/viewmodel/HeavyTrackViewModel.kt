package com.example.ui.viewmodel

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.model.Supervisor
import com.example.data.model.SupervisorDirectory
import com.example.data.remote.ActivityPayload
import com.example.data.remote.CompanyServerSimulator
import com.example.data.remote.KendalaItemPayload
import com.example.data.remote.KendalaJsonConverter
import com.example.data.remote.OperatorPayload
import com.example.data.remote.UnitPayload
import com.example.data.repository.HeavyTrackRepository
import com.example.data.repository.SyncLogItem
import com.example.util.ImageCompressor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FormUiState(
    val reportDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val selectedUnit: UnitEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedOperator: OperatorEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val workLocation: String = "001A",
    val shift: String = "SIANG",
    val workDurationHours: Double = 7.0,
    val hmAwal: Double = 0.0,
    val hmAkhirInput: String = "",
    val hmDailyRunning: Double = 0.0,
    val isHmValid: Boolean = true,
    val hmErrorMessage: String? = null,
    val workOutputInput: String = "",
    val notes: String = "",
    val unitStatus: String = "OPERASI",  // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val isRefueled: Boolean = false,
    val fuelLitersInput: String = "",
    val standbyReason: String = "Hujan / Slippery",
    val breakdownTrouble: String = "Trouble Engine / Mesin",
    val photoPath: String? = null,
    val photoBase64: String? = null,
    val photoSizeKb: Long? = null,
    val photoDimensions: String? = null,
    val isCompressingPhoto: Boolean = false,
    val isSubmitting: Boolean = false,
    val formError: String? = null
)

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
    data class Info(val message: String) : UiMessage
}

class HeavyTrackViewModel(
    private val repository: HeavyTrackRepository
) : ViewModel() {

    companion object {
        const val INTERNAL_WEB_ADMIN_URL = "https://1-portalce.ai.studio/"
        const val GITHUB_REPO_URL = "https://github.com/tiaprima8-png/tes"
        const val APK_DIRECT_DOWNLOAD_URL = "https://github.com/tiaprima8-png/tes/releases/latest/download/app-release.apk"
    }

    // Configurable APK Download URL (dengan fallback ke GitHub Release resmi)
    private val _customApkUrl = MutableStateFlow(APK_DIRECT_DOWNLOAD_URL)
    val customApkUrl: StateFlow<String> = _customApkUrl.asStateFlow()

    fun updateCustomApkUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        if (trimmed.isNotEmpty()) {
            _customApkUrl.value = trimmed
        }
    }

    // Active Supervisor
    private val _activeSupervisor = MutableStateFlow("Budi Santoso")
    val activeSupervisor: StateFlow<String> = _activeSupervisor.asStateFlow()

    // Supervisor Login Session
    private val _currentSupervisorSession = MutableStateFlow<Supervisor?>(null)
    val currentSupervisorSession: StateFlow<Supervisor?> = _currentSupervisorSession.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Base URL configuration for Web Admin (Internalized)
    val apiBaseUrl: String = INTERNAL_WEB_ADMIN_URL

    // Offline mode simulation toggle
    private val _isOfflineSimulated = MutableStateFlow(CompanyServerSimulator.isSimulatedOffline)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    // Sync logs
    val syncLogs: StateFlow<List<SyncLogItem>> = repository.syncLogs

    // UI events (snackbars)
    private val _uiEvents = MutableSharedFlow<UiMessage>()
    val uiEvents: SharedFlow<UiMessage> = _uiEvents.asSharedFlow()

    // Syncing state
    private val _isSyncingMaster = MutableStateFlow(false)
    val isSyncingMaster: StateFlow<Boolean> = _isSyncingMaster.asStateFlow()

    private val _isSyncingPending = MutableStateFlow(false)
    val isSyncingPending: StateFlow<Boolean> = _isSyncingPending.asStateFlow()

    val lastSyncTimeMillis: StateFlow<Long> = repository.lastSyncTimeMillis

    // Master Units (filtered by supervisor)
    val units: StateFlow<List<UnitEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getUnitsForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Activities
    val activities: StateFlow<List<ActivityMasterEntity>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Operators (filtered by supervisor)
    val operators: StateFlow<List<OperatorEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getOperatorsForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Lokasi Kerja
    val lokasiList: StateFlow<List<LokasiEntity>> = repository.getAllLokasi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master Kendala Lapangan
    val kendalaList: StateFlow<List<KendalaEntity>> = repository.getAllKendala()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports list (historical)
    val reports: StateFlow<List<ActivityReportEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getReportsForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Rencana Kerja (shift today / active and NOT yet realized)
    val activeRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getActiveRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selector Unit: Deteksi Backlog (Hari Kemarin) & Filter Otomatis (Unit Selesai Otomatis Hilang)
    val availableUnitsForRencana: StateFlow<List<UnitPlanStatus>> = combine(
        units,
        allRencanaList
    ) { unitList, rencanaList ->
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val yesterdayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))

        unitList.mapNotNull { u ->
            // Apakah unit sudah dibuatkan rencana kerja untuk hari kemarin?
            val hasYesterday = rencanaList.any {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == yesterdayStr
            }
            // Apakah unit sudah dibuatkan rencana kerja untuk hari ini?
            val hasToday = rencanaList.any {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == todayStr
            }

            if (!hasYesterday) {
                // 1. Deteksi Backlog: Kemarin belum diisi. Pengawas wajib menyelesaikan ini terlebih dahulu
                UnitPlanStatus(
                    unit = u,
                    targetDate = yesterdayStr,
                    isBacklog = true,
                    labelText = "${u.unitCode} - [Tanggal Kemarin: $yesterdayStr - Belum Diisi]"
                )
            } else if (!hasToday) {
                // 2. Input Hari Ini: Kemarin sudah beres, hari ini belum diinput
                UnitPlanStatus(
                    unit = u,
                    targetDate = todayStr,
                    isBacklog = false,
                    labelText = "${u.unitCode} - [Hari Ini: $todayStr]"
                )
            } else {
                // 3. Filter Otomatis: Sudah selesai hari ini & tidak ada backlog -> Otomatis hilang dari dropdown
                null
            }
        }.sortedWith(
            compareByDescending<UnitPlanStatus> { it.isBacklog }.thenBy { it.unit.unitCode }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hasil Input Realisasi
    val hasilList: StateFlow<List<HasilInputAktivitasEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getHasilForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unsynced count
    val unsyncedCount: StateFlow<Int> = repository.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ==========================================
    // TAHAP FORMULIR (SUB-TAB: 0 RENCANA, 1 REALISASI)
    // ==========================================
    private val _formSubTab = MutableStateFlow(0)
    val formSubTab: StateFlow<Int> = _formSubTab.asStateFlow()

    private val _rencanaFormState = MutableStateFlow(RencanaFormUiState())
    val rencanaFormState: StateFlow<RencanaFormUiState> = _rencanaFormState.asStateFlow()

    private val _realisasiFormState = MutableStateFlow(RealisasiFormUiState())
    val realisasiFormState: StateFlow<RealisasiFormUiState> = _realisasiFormState.asStateFlow()

    // Backward-compatible single form state
    private val _formState = MutableStateFlow(FormUiState())
    val formState: StateFlow<FormUiState> = _formState.asStateFlow()

    // ==========================================
    // FLEET ANALYTICS STATE & FILTERS
    // ==========================================
    private val _analyticsPeriod = MutableStateFlow("Bulan Ini")
    val analyticsPeriod: StateFlow<String> = _analyticsPeriod.asStateFlow()

    private val _analyticsUnitFilter = MutableStateFlow("Semua Unit")
    val analyticsUnitFilter: StateFlow<String> = _analyticsUnitFilter.asStateFlow()

    val analyticsState: StateFlow<FleetAnalyticsState> = combine(
        reports,
        _analyticsPeriod,
        _analyticsUnitFilter
    ) { reportList, period, unitFilter ->
        calculateAnalytics(reportList, period, unitFilter)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FleetAnalyticsState()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(_activeSupervisor.value)
            fetchMasterData()
        }

        // Live WebSocket / Polling simulation listener for SPK_PUBLISHED events
        viewModelScope.launch {
            CompanyServerSimulator.spkEventFlow.collect { event ->
                repository.updateRencanaSpk(event.planId, event.nomorSpk, "TERBIT_SPK")
                val curSelected = _realisasiFormState.value.selectedRencana
                if (curSelected != null && curSelected.id == event.planId) {
                    _realisasiFormState.value = _realisasiFormState.value.copy(
                        nomorSpk = event.nomorSpk,
                        selectedRencana = curSelected.copy(
                            nomorSpk = event.nomorSpk,
                            statusSpk = "TERBIT_SPK"
                        )
                    )
                    _uiEvents.emit(
                        UiMessage.Success("Nomor SPK Resmi Diterbitkan Web Admin: ${event.nomorSpk} (Terkunci)")
                    )
                }
            }
        }

        // Polling loop to sync any published SPK in background
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(4000)
                val curSelected = _realisasiFormState.value.selectedRencana
                if (curSelected != null && _realisasiFormState.value.nomorSpk.isBlank()) {
                    val published = CompanyServerSimulator.getPublishedSpk(curSelected.id)
                    if (!published.isNullOrBlank()) {
                        repository.updateRencanaSpk(curSelected.id, published, "TERBIT_SPK")
                        _realisasiFormState.value = _realisasiFormState.value.copy(
                            nomorSpk = published,
                            selectedRencana = curSelected.copy(
                                nomorSpk = published,
                                statusSpk = "TERBIT_SPK"
                            )
                        )
                        _uiEvents.emit(
                            UiMessage.Success("Nomor SPK Resmi: $published (Terkunci Otomatis)")
                        )
                    }
                }
            }
        }
    }

    fun setFormSubTab(tab: Int) {
        _formSubTab.value = tab
    }

    // ==========================================
    // LOGIN & SESI
    // ==========================================
    fun login(supervisor: Supervisor) {
        _currentSupervisorSession.value = supervisor
        _activeSupervisor.value = supervisor.name
        _isLoggedIn.value = true
        _rencanaFormState.value = _rencanaFormState.value.copy(
            shiftKerja = supervisor.defaultShift
        )
        viewModelScope.launch {
            _uiEvents.emit(UiMessage.Success("Selamat bertugas, Pengawas ${supervisor.name} (${supervisor.nik})"))
            fetchMasterData()
            syncPendingReports()
        }
    }

    fun loginManual(username: String, pin: String): Boolean {
        val trimmedUser = username.trim()
        val trimmedPin = pin.trim()
        val matched = SupervisorDirectory.authenticate(trimmedUser, trimmedPin)
        if (matched != null) {
            login(matched)
            return true
        }

        // Field standalone fallback for newly deployed supervisors
        if (trimmedUser.isNotBlank() && (trimmedPin == "1234" || trimmedPin.length >= 4)) {
            val fallbackSpv = Supervisor(
                id = "SPV-${System.currentTimeMillis().toString().takeLast(3)}",
                name = trimmedUser,
                nik = trimmedUser.uppercase(),
                passwordPin = trimmedPin,
                defaultShift = "SIANG",
                assignedSector = "Sektor Operasional Lapangan",
                phone = "+62 812-0000-0000"
            )
            login(fallbackSpv)
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentSupervisorSession.value = null
        viewModelScope.launch {
            _uiEvents.emit(UiMessage.Info("Anda telah keluar dari sesi pengawas"))
        }
    }

    fun switchSupervisor(newSupervisor: String) {
        _activeSupervisor.value = newSupervisor
        fetchMasterData()
    }

    fun toggleOfflineSimulation() {
        val next = !_isOfflineSimulated.value
        CompanyServerSimulator.isSimulatedOffline = next
        _isOfflineSimulated.value = next
        viewModelScope.launch {
            if (next) {
                _uiEvents.emit(UiMessage.Info("Mode Offline Lapangan Aktif (Data tersimpan di SQLite lokal)"))
            } else {
                _uiEvents.emit(UiMessage.Success("Koneksi Pulih! Mengirim antrean offline ke Web Admin..."))
                syncPendingReports()
            }
        }
    }

    // ==========================================
    // AUTO-SYNC & MASTER DATA
    // ==========================================
    fun fetchMasterData() {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            val result = repository.fetchMasterData(_activeSupervisor.value)
            _isSyncingMaster.value = false
            result.onSuccess { msg ->
                _uiEvents.emit(UiMessage.Success("Auto-Sync: $msg"))
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Info("Offline: ${err.message ?: "Data dimuat dari SQLite lokal"}"))
            }
        }
    }

    fun syncPendingReports() {
        viewModelScope.launch {
            _isSyncingPending.value = true
            val result = repository.syncAllPendingOperationalData()
            _isSyncingPending.value = false
            result.onSuccess { count ->
                if (count > 0) {
                    _uiEvents.emit(UiMessage.Success("Berhasil menyinkronkan $count entri operasional ke Web Admin!"))
                } else {
                    _uiEvents.emit(UiMessage.Info("Semua data operasional telah sinkron."))
                }
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal sinkron antrean: ${err.message}"))
            }
        }
    }

    fun getLastSyncDescription(): String {
        val lastTime = lastSyncTimeMillis.value
        val now = System.currentTimeMillis()
        val diffMinutes = (now - lastTime) / (60 * 1000)
        return when {
            diffMinutes <= 1 -> "Baru saja"
            diffMinutes < 60 -> "$diffMinutes menit yang lalu"
            else -> {
                val hours = diffMinutes / 60
                "$hours jam yang lalu"
            }
        }
    }

    // ==========================================
    // TAHAP 1: FORMULIR RENCANA KERJA
    // ==========================================
    fun setRencanaStatus(status: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(statusUnit = status)
    }

    fun selectRencanaUnitStatus(status: UnitPlanStatus) {
        _rencanaFormState.value = _rencanaFormState.value.copy(
            selectedUnitStatus = status,
            selectedUnit = status.unit,
            targetDate = status.targetDate,
            isBacklog = status.isBacklog
        )
    }

    fun selectRencanaUnit(unit: UnitEntity) {
        // Backward compatibility
        val matchedStatus = availableUnitsForRencana.value.find { it.unit.unitCode.equals(unit.unitCode, ignoreCase = true) }
        if (matchedStatus != null) {
            selectRencanaUnitStatus(matchedStatus)
        } else {
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            _rencanaFormState.value = _rencanaFormState.value.copy(
                selectedUnit = unit,
                targetDate = todayStr,
                isBacklog = false
            )
        }
    }

    fun selectRencanaOperator(operator: OperatorEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedOperator = operator)
    }

    fun selectRencanaActivity(activity: ActivityMasterEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedActivity = activity)
    }

    fun selectRencanaLokasi(lokasi: LokasiEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedLokasi = lokasi)
    }

    fun setRencanaShift(shift: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(shiftKerja = shift)
    }

    fun setRencanaKeterangan(ket: String) {
        _rencanaFormState.value = _rencanaFormState.value.copy(keterangan = ket)
    }

    // Sub-Form Multi-Rencana (1 Unit & 1 Tanggal bisa > 1 Rencana Kerja)
    fun addSubRencana() {
        val current = _rencanaFormState.value
        val newSub = SubRencanaFormState(
            shiftKerja = current.shiftKerja
        )
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans + newSub
        )
    }

    fun removeSubRencana(id: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.filter { it.id != id }
        )
    }

    fun updateSubRencanaOperator(id: String, op: OperatorEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedOperator = op) else it }
        )
    }

    fun updateSubRencanaActivity(id: String, act: ActivityMasterEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedActivity = act) else it }
        )
    }

    fun updateSubRencanaLokasi(id: String, lok: LokasiEntity) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(selectedLokasi = lok) else it }
        )
    }

    fun updateSubRencanaShift(id: String, shift: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(shiftKerja = shift) else it }
        )
    }

    fun updateSubRencanaKeterangan(id: String, ket: String) {
        val current = _rencanaFormState.value
        _rencanaFormState.value = current.copy(
            subPlans = current.subPlans.map { if (it.id == id) it.copy(keterangan = ket) else it }
        )
    }

    fun submitRencanaKerja() {
        val state = _rencanaFormState.value
        val unit = state.selectedUnit
        val op = state.selectedOperator
        val act = state.selectedActivity
        val lok = state.selectedLokasi

        if (unit == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Unit Alat Berat terlebih dahulu")) }
            return
        }
        if (op == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Operator Pelaksana pada Rencana Utama")) }
            return
        }
        if (act == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Aktivitas / Jenis Pekerjaan pada Rencana Utama")) }
            return
        }
        if (lok == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Lokasi Kerja Lapangan pada Rencana Utama")) }
            return
        }

        // Validasi kelengkapan seluruh sub-form jika ditambahkan
        state.subPlans.forEachIndexed { idx, sub ->
            if (sub.selectedOperator == null || sub.selectedActivity == null || sub.selectedLokasi == null) {
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage.Error("Rencana Tambahan #${idx + 1} belum lengkap. Mohon pilih Operator, Aktivitas, dan Lokasi."))
                }
                return
            }
        }

        val targetDate = if (state.targetDate.isNotBlank()) {
            state.targetDate
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        viewModelScope.launch {
            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = true)

            // 1. Rencana Utama
            val primaryRencana = RencanaKerjaEntity(
                supervisorName = _activeSupervisor.value,
                statusUnit = state.statusUnit,
                kodeUnit = unit.unitCode,
                operator = op.name,
                sapCode = act.sapCode,
                namaAktivitas = act.activityName,
                satuanAktivitas = act.unitOfMeasure,
                kodeLokasi = lok.kodeLokasi,
                shiftKerja = state.shiftKerja,
                keterangan = state.keterangan,
                nomorSpk = "",
                statusSpk = "MENUNGGU_SPK",
                statusTahap = "RENCANA",
                isRealized = false,
                createdAt = System.currentTimeMillis(),
                tanggalFormat = targetDate
            )

            val primaryResult = repository.submitRencanaKerja(primaryRencana)

            // 2. Simpan Sub-Rencana Tambahan
            var subSuccessCount = 0
            state.subPlans.forEach { sub ->
                val subRencana = RencanaKerjaEntity(
                    supervisorName = _activeSupervisor.value,
                    statusUnit = state.statusUnit,
                    kodeUnit = unit.unitCode,
                    operator = sub.selectedOperator?.name ?: op.name,
                    sapCode = sub.selectedActivity?.sapCode ?: "",
                    namaAktivitas = sub.selectedActivity?.activityName ?: "",
                    satuanAktivitas = sub.selectedActivity?.unitOfMeasure ?: "m3",
                    kodeLokasi = sub.selectedLokasi?.kodeLokasi ?: lok.kodeLokasi,
                    shiftKerja = sub.shiftKerja,
                    keterangan = sub.keterangan,
                    nomorSpk = "",
                    statusSpk = "MENUNGGU_SPK",
                    statusTahap = "RENCANA",
                    isRealized = false,
                    createdAt = System.currentTimeMillis() + 1,
                    tanggalFormat = targetDate
                )
                repository.submitRencanaKerja(subRencana)
                subSuccessCount++
            }

            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = false)

            primaryResult.onSuccess { saved ->
                val totalPlans = 1 + subSuccessCount
                val backlogNotice = if (state.isBacklog) " [Backlog Tertinggal Terselesaikan]" else ""
                _uiEvents.emit(
                    UiMessage.Success("Berhasil membuat $totalPlans Rencana Kerja untuk Unit ${saved.kodeUnit} ($targetDate)$backlogNotice")
                )
                // Reset form
                _rencanaFormState.value = RencanaFormUiState(
                    shiftKerja = state.shiftKerja
                )
                // Otomatis beralih ke Tahap 2 Realisasi Kerja dengan rencana utama yang baru dibuat
                selectRencanaForRealisasi(saved)
                _formSubTab.value = 1
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan rencana: ${err.message}"))
            }
        }
    }

    // ==========================================
    // TAHAP 2: FORMULIR REALISASI KERJA
    // ==========================================
    fun selectRencanaForRealisasi(rencana: RencanaKerjaEntity) {
        val unitEntity = units.value.find { it.unitCode.equals(rencana.kodeUnit, ignoreCase = true) }
        val hmAwalVal = unitEntity?.lastHm ?: 0.0
        val minStd = unitEntity?.hmMinStandar ?: 6.0
        val maxStd = unitEntity?.hmMaxStandar ?: 10.0

        val matchedActivity = activities.value.find {
            it.sapCode.equals(rencana.sapCode, ignoreCase = true) ||
            it.activityName.equals(rencana.namaAktivitas, ignoreCase = true)
        } ?: if (rencana.sapCode.isNotBlank()) {
            ActivityMasterEntity(
                sapCode = rencana.sapCode,
                activityName = rencana.namaAktivitas.ifBlank { "Aktivitas Lapangan" },
                unitOfMeasure = rencana.satuanAktivitas.ifBlank { "m3" }
            )
        } else null

        val matchedLokasi = lokasiList.value.find {
            it.kodeLokasi.equals(rencana.kodeLokasi, ignoreCase = true)
        } ?: if (rencana.kodeLokasi.isNotBlank()) {
            LokasiEntity(
                kodeLokasi = rencana.kodeLokasi,
                wilayah = "Aktual",
                luasBruto = 0.0,
                luasNetto = 0.0
            )
        } else null

        val spkFromMem = CompanyServerSimulator.getPublishedSpk(rencana.id) ?: ""
        val resolvedSpk = if (rencana.nomorSpk.isNotBlank()) rencana.nomorSpk else spkFromMem

        _realisasiFormState.value = _realisasiFormState.value.copy(
            selectedRencana = rencana,
            selectedActivity = matchedActivity ?: _realisasiFormState.value.selectedActivity,
            selectedLokasi = matchedLokasi ?: _realisasiFormState.value.selectedLokasi,
            nomorSpk = resolvedSpk,
            hmAwal = hmAwalVal,
            hmAkhirInput = "",
            hmHarianBerjalan = 0.0,
            isHmValid = true,
            hmErrorMessage = null,
            hmMinStandar = minStd,
            hmMaxStandar = maxStd,
            isUnderMinStandard = false,
            selectedKendala = null,
            catatanKendala = "",
            stikSolarAwalInput = "",
            stikSolarAkhirInput = "",
            isIsiSolar = false,
            jumlahLiterSolarInput = "",
            pemakaianSolarOtomatis = 0.0
        )
    }

    fun adminSimulatePublishSpk(planId: Long, unitCode: String) {
        viewModelScope.launch {
            val spk = CompanyServerSimulator.adminPublishSpk(planId, unitCode, _activeSupervisor.value)
            repository.updateRencanaSpk(planId, spk, "TERBIT_SPK")
            val cur = _realisasiFormState.value.selectedRencana
            if (cur != null && cur.id == planId) {
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    nomorSpk = spk,
                    selectedRencana = cur.copy(nomorSpk = spk, statusSpk = "TERBIT_SPK")
                )
            }
            _uiEvents.emit(UiMessage.Success("Web Admin telah menerbitkan SPK: $spk"))
        }
    }

    fun setRealisasiActivity(activity: ActivityMasterEntity) {
        _realisasiFormState.value = _realisasiFormState.value.copy(selectedActivity = activity)
    }

    fun setRealisasiLokasi(lokasi: LokasiEntity) {
        _realisasiFormState.value = _realisasiFormState.value.copy(selectedLokasi = lokasi)
    }

    fun setRealisasiNomorSpk(spk: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(nomorSpk = spk)
    }

    fun setRealisasiDurasiJam(durasi: Double) {
        _realisasiFormState.value = _realisasiFormState.value.copy(durasiJamKerja = durasi)
    }

    fun setRealisasiHmAkhir(input: String) {
        val state = _realisasiFormState.value
        val hmAwal = state.hmAwal
        val cleanInput = input.replace(',', '.')
        val inputDouble = cleanInput.toDoubleOrNull()

        val (isValid, running, errorMsg) = validateHm(hmAwal, inputDouble)
        val isUnderMin = inputDouble != null && isValid && running > 0.0 && running < state.hmMinStandar
        val kendalaList = if (isUnderMin && state.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else state.daftarKendala

        _realisasiFormState.value = state.copy(
            hmAkhirInput = cleanInput,
            isHmValid = isValid,
            hmHarianBerjalan = running,
            hmErrorMessage = errorMsg,
            isUnderMinStandard = isUnderMin,
            daftarKendala = kendalaList
        )
        recalculateSolarConsumption()
    }

    fun quickAddRealisasiHours(hours: Double) {
        val hmAwal = _realisasiFormState.value.hmAwal
        val newHmAkhir = hmAwal + hours
        setRealisasiHmAkhir(String.format(Locale.US, "%.1f", newHmAkhir))
    }

    // Parameter Stik Solar BBM
    fun setRealisasiStikAwal(input: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            stikSolarAwalInput = input.replace(',', '.')
        )
        recalculateSolarConsumption()
    }

    fun setRealisasiStikAkhir(input: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            stikSolarAkhirInput = input.replace(',', '.')
        )
        recalculateSolarConsumption()
    }

    fun setRealisasiIsiSolar(isIsi: Boolean) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            isIsiSolar = isIsi,
            jumlahLiterSolarInput = if (!isIsi) "" else _realisasiFormState.value.jumlahLiterSolarInput
        )
        recalculateSolarConsumption()
    }

    fun setRealisasiJumlahSolar(liters: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            jumlahLiterSolarInput = liters.replace(',', '.')
        )
        recalculateSolarConsumption()
    }

    private fun recalculateSolarConsumption() {
        val state = _realisasiFormState.value
        val awal = state.stikSolarAwalInput.toDoubleOrNull() ?: 0.0
        val akhir = state.stikSolarAkhirInput.toDoubleOrNull() ?: 0.0
        val isi = if (state.isIsiSolar) (state.jumlahLiterSolarInput.toDoubleOrNull() ?: 0.0) else 0.0
        val delta = (awal - akhir).coerceAtLeast(0.0)
        val total = delta + isi
        _realisasiFormState.value = _realisasiFormState.value.copy(
            pemakaianSolarOtomatis = total
        )
    }

    // Master Kendala Dropdown Selection (Multi-Kendala Support)
    fun selectRealisasiKendala(kendala: KendalaEntity) {
        updateKendalaSelection(0, kendala)
    }

    fun addKendalaRow() {
        val current = _realisasiFormState.value.daftarKendala
        _realisasiFormState.value = _realisasiFormState.value.copy(
            daftarKendala = current + KendalaItemFormState()
        )
    }

    fun removeKendalaRow(index: Int) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            if (current.isEmpty()) {
                current.add(KendalaItemFormState())
            }
            _realisasiFormState.value = _realisasiFormState.value.copy(
                daftarKendala = current,
                selectedKendala = current.firstOrNull()?.selectedKendala
            )
        }
    }

    fun updateKendalaSelection(index: Int, kendala: KendalaEntity) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(selectedKendala = kendala)
            _realisasiFormState.value = _realisasiFormState.value.copy(
                daftarKendala = current,
                selectedKendala = current.firstOrNull()?.selectedKendala
            )
        }
    }

    fun updateKendalaWaktuMulai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuMulai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
        }
    }

    fun updateKendalaWaktuSelesai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuSelesai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
        }
    }

    fun setRealisasiCatatanKendala(notes: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(catatanKendala = notes)
    }

    fun setRealisasiHasilKerja(hasil: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(hasilKerjaInput = hasil)
    }

    fun setRealisasiKeterangan(ket: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(keterangan = ket)
    }

    fun processAndCompressRealisasiPhoto(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _realisasiFormState.value = _realisasiFormState.value.copy(isCompressingPhoto = true)
            val result = ImageCompressor.compressImage(context, imageUri)
            _realisasiFormState.value = _realisasiFormState.value.copy(isCompressingPhoto = false)
            result.onSuccess { compressed ->
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    photoPath = compressed.filePath,
                    photoBase64 = compressed.base64,
                    photoSizeKb = compressed.sizeKb,
                    photoDimensions = compressed.dimensionsText
                )
                _uiEvents.emit(
                    UiMessage.Success("Foto bukti terkompresi: ${compressed.sizeKb} KB (${compressed.dimensionsText})")
                )
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal kompresi foto: ${err.message}"))
            }
        }
    }

    fun removeRealisasiPhoto() {
        val old = _realisasiFormState.value.photoPath
        if (!old.isNullOrBlank()) {
            try { File(old).delete() } catch (_: Exception) {}
        }
        _realisasiFormState.value = _realisasiFormState.value.copy(
            photoPath = null,
            photoBase64 = null,
            photoSizeKb = null,
            photoDimensions = null
        )
    }

    fun submitRealisasiKerja() {
        val state = _realisasiFormState.value
        val rencana = state.selectedRencana

        if (rencana == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Rencana Kerja aktif terlebih dahulu")) }
            return
        }

        val hmAkhir = state.hmAkhirInput.toDoubleOrNull()
        if (hmAkhir == null || hmAkhir < state.hmAwal) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("HM Akhir wajib diisi dan >= HM Awal (${String.format(Locale.US, "%.1f", state.hmAwal)})"))
            }
            return
        }

        val runningHm = hmAkhir - state.hmAwal
        val isUnderMin = runningHm > 0.0 && runningHm < state.hmMinStandar

        // Validasi HM Kurang dari Standar Minimal:
        // Wajib minimal 1 kendala lengkap dengan waktu mulai & selesai (waktu selesai > waktu mulai)
        if (isUnderMin) {
            if (state.daftarKendala.isEmpty() || !state.daftarKendala.all { it.isValid }) {
                viewModelScope.launch {
                    val invalidReason = when {
                        state.daftarKendala.isEmpty() -> "Wajib mencatat minimal 1 kendala lapangan."
                        state.daftarKendala.any { it.selectedKendala == null } -> "Penyebab kendala wajib dipilih dari Master Kendala."
                        state.daftarKendala.any { it.waktuMulai.isBlank() || it.waktuSelesai.isBlank() } -> "Waktu mulai dan selesai kendala wajib diisi (JJ:MM)."
                        state.daftarKendala.any { !it.isTimeRangeValid } -> "Waktu selesai kendala harus lebih besar dari waktu mulai."
                        else -> "Lengkapi data kendala dan rentang waktu kejadian."
                    }
                    _uiEvents.emit(
                        UiMessage.Error("HM Berjalan (${String.format(Locale.US, "%.1f", runningHm)} HM) di bawah standar (${state.hmMinStandar} HM). $invalidReason")
                    )
                }
                return
            }
        }

        val fuelLiters = if (state.isIsiSolar) {
            val parsed = state.jumlahLiterSolarInput.toDoubleOrNull()
            if (parsed == null || parsed <= 0.0) {
                viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pengisian solar aktif: Isi Jumlah Liter Solar (> 0)")) }
                return
            }
            parsed
        } else {
            0.0
        }

        val workOutput = if (rencana.statusUnit == "OPERASI") {
            state.hasilKerjaInput.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        // Fleksibilitas Kondisi Lapangan: Gunakan Aktivitas & Lokasi aktual jika diedit oleh pengawas
        val actName = state.selectedActivity?.activityName
            ?: rencana.namaAktivitas.ifBlank { "Aktivitas Operasi Lapangan" }
        val sapCode = state.selectedActivity?.sapCode
            ?: rencana.sapCode.ifBlank { "ACT-OPR-01" }
        val satuan = state.selectedActivity?.unitOfMeasure
            ?: rencana.satuanAktivitas.ifBlank { "m3" }
        val lokasiAktual = state.selectedLokasi?.kodeLokasi
            ?: rencana.kodeLokasi

        val stikAwal = state.stikSolarAwalInput.toDoubleOrNull() ?: 0.0
        val stikAkhir = state.stikSolarAkhirInput.toDoubleOrNull() ?: 0.0

        val validKendalaList: List<KendalaItemFormState> = if (isUnderMin) {
            state.daftarKendala.filter { it.isValid }
        } else {
            emptyList()
        }

        val kendalaPayloads = validKendalaList.map { item ->
            KendalaItemPayload(
                id_kendala = item.selectedKendala?.kodeKendala,
                nama_kendala = item.selectedKendala?.namaKendala,
                waktu_mulai = item.waktuMulai,
                waktu_selesai = item.waktuSelesai,
                durasi_menit = item.durasiMenit ?: 0
            )
        }

        val daftarKendalaJson = if (kendalaPayloads.isNotEmpty()) {
            KendalaJsonConverter.toJson(kendalaPayloads)
        } else {
            null
        }

        val primaryKendala = validKendalaList.firstOrNull()?.selectedKendala
        val kendalaSummaryName = if (validKendalaList.isNotEmpty()) {
            validKendalaList.joinToString(", ") { "${it.selectedKendala?.namaKendala} (${it.waktuMulai}-${it.waktuSelesai})" }
        } else {
            null
        }
        val kendalaSummaryCatatan = if (validKendalaList.isNotEmpty()) {
            "Total ${validKendalaList.size} Kendala [Durasi: ${state.totalHambatanLabel}]: " +
                    validKendalaList.joinToString("; ") {
                        "${it.selectedKendala?.namaKendala} (${it.waktuMulai} - ${it.waktuSelesai}, ${it.durasiLabel})"
                    }
        } else {
            null
        }

        viewModelScope.launch {
            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = true)

            val hasilEntity = HasilInputAktivitasEntity(
                rencanaId = rencana.id,
                nomorSpk = state.nomorSpk,
                supervisorName = _activeSupervisor.value,
                kodeUnit = rencana.kodeUnit,
                operator = rencana.operator,
                kodeLokasi = lokasiAktual,
                activityName = actName,
                sapCode = sapCode,
                satuan = satuan,
                unitStatus = rencana.statusUnit,
                shift = rencana.shiftKerja,
                jamKerja = state.durasiJamKerja,
                hmAwal = state.hmAwal,
                hmAkhir = hmAkhir,
                hmHarianBerjalan = runningHm,
                isIsiSolar = state.isIsiSolar,
                jumlahLiterSolar = fuelLiters,
                stikSolarAwal = stikAwal,
                stikSolarAkhir = stikAkhir,
                pemakaianSolar = state.pemakaianSolarOtomatis,
                kendalaId = primaryKendala?.kodeKendala,
                kendalaNama = kendalaSummaryName,
                catatanKendala = kendalaSummaryCatatan,
                daftarKendalaJson = daftarKendalaJson,
                hasilKerja = workOutput,
                keterangan = state.keterangan,
                photoPath = state.photoPath,
                photoBase64 = state.photoBase64,
                photoSizeKb = state.photoSizeKb,
                photoDimensions = state.photoDimensions,
                createdAt = System.currentTimeMillis(),
                tanggalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )

            val result = repository.submitHasilAktivitas(hasilEntity)
            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = false)

            result.onSuccess { saved ->
                val fuelTag = if (saved.isIsiSolar) " [Solar: ${saved.jumlahLiterSolar}L]" else ""
                val spkTag = if (saved.nomorSpk.isNotBlank()) " (SPK: ${saved.nomorSpk})" else ""
                val kendalaTag = if (!saved.kendalaNama.isNullOrBlank()) " [Kendala: ${saved.kendalaNama}]" else ""
                _uiEvents.emit(
                    UiMessage.Success("Realisasi Unit ${saved.kodeUnit}$spkTag$fuelTag$kendalaTag berhasil dikirim & disimpan!")
                )

                // Reset realisasi form
                _realisasiFormState.value = RealisasiFormUiState()
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan realisasi: ${err.message}"))
            }
        }
    }

    private fun validateHm(hmAwal: Double, hmAkhir: Double?): Triple<Boolean, Double, String?> {
        if (hmAkhir == null) {
            return Triple(true, 0.0, null)
        }
        return if (hmAkhir < hmAwal) {
            val diff = hmAwal - hmAkhir
            Triple(
                false,
                0.0,
                "HM Akhir (${String.format(Locale.US, "%.1f", hmAkhir)}) lebih kecil dari HM Awal (${String.format(Locale.US, "%.1f", hmAwal)}) selisih -${String.format(Locale.US, "%.1f", diff)}"
            )
        } else {
            val running = hmAkhir - hmAwal
            Triple(true, running, null)
        }
    }

    // ==========================================
    // MODUL DISTRIBUSI & PEMASANGAN INSTALLER APK
    // ==========================================
    fun getOrExtractApkFile(context: Context): File? {
        return try {
            val sourcePath = context.applicationInfo.sourceDir
            if (sourcePath.isNullOrEmpty()) return null
            val sourceApk = File(sourcePath)
            if (!sourceApk.exists() || sourceApk.length() == 0L) return null

            val cacheShareDir = File(context.cacheDir, "apk_share").apply { mkdirs() }
            val targetApk = File(cacheShareDir, "RKCE_Installer_v1.0.apk")
            if (!targetApk.exists() || targetApk.length() != sourceApk.length()) {
                sourceApk.copyTo(targetApk, overwrite = true)
            }
            targetApk
        } catch (_: Exception) {
            null
        }
    }

    fun downloadApk(context: Context) {
        viewModelScope.launch {
            try {
                var savedLocally = false
                val sourcePath = context.applicationInfo.sourceDir
                val sourceApk = if (!sourcePath.isNullOrEmpty()) File(sourcePath) else null

                if (sourceApk != null && sourceApk.exists() && sourceApk.length() > 0L) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "RKCE_Installer_v1.0.apk")
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { output ->
                                sourceApk.inputStream().use { input -> input.copyTo(output) }
                            }
                            savedLocally = true
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        downloadsDir.mkdirs()
                        val destFile = File(downloadsDir, "RKCE_Installer_v1.0.apk")
                        sourceApk.copyTo(destFile, overwrite = true)
                        savedLocally = true
                    }
                }

                if (savedLocally) {
                    _uiEvents.emit(
                        UiMessage.Success(
                            "Installer asli 'RKCE_Installer_v1.0.apk' berhasil disimpan di folder Download! Berkas utuh dan siap dipasang atau dikirim ke HP lain."
                        )
                    )
                } else {
                    // Fallback unduh via DownloadManager dari URL resmi
                    val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                    val request = DownloadManager.Request(Uri.parse(downloadUrl))
                        .setTitle("RKCE_Installer_v1.0.apk")
                        .setDescription("Mengunduh installer resmi RKCE (Rencana Kerja Civil Engineering)...")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "RKCE_Installer_v1.0.apk")
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    dm?.enqueue(request)
                    _uiEvents.emit(UiMessage.Success("Memulai unduhan berkas APK resmi ke folder Download perangkat..."))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan installer: ${e.localizedMessage ?: "Periksa izin penyimpanan"}"))
            }
        }
    }

    fun shareApkViaWhatsApp(context: Context) {
        shareApk(context, targetWhatsAppOnly = true)
    }

    fun shareApkGeneral(context: Context) {
        shareApk(context, targetWhatsAppOnly = false)
    }

    private fun shareApk(context: Context, targetWhatsAppOnly: Boolean) {
        viewModelScope.launch {
            try {
                val apkFile = getOrExtractApkFile(context)
                val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                val shareCaption = """
*Aplikasi RKCE (Rencana Kerja Unit Civil Engineering) v1.0*

Halo Rekan Pengawas, berkas installer APK resmi terlampir. Anda dapat langsung memasang aplikasi ini di HP Android Anda.

*PANDUAN AGAR BISA DIINSTAL DI HP LAIN:*
1. Buka atau klik berkas *RKCE_Installer_v1.0.apk* yang terlampir.
2. Jika muncul peringatan keamanan sistem Android, pilih *'Setelan' / 'Pengaturan'* lalu aktifkan *'Izinkan dari sumber ini'*.
3. Jika muncul notifikasi Google Play Protect ('Aplikasi tidak dikenal' / 'Blocked by Play Protect'):
   👉 Klik *'Detail Lainnya'* (More details)
   👉 Pilih *'Tetap Instal'* (Install anyway).
4. Jika sebelumnya sudah ada versi RKCE yang terpasang namun gagal update, copot (uninstall) dulu versi lama tersebut lalu pasang kembali berkas ini.

*Tautan Unduh Resmi (GitHub):*
$downloadUrl
                """.trimIndent()

                if (apkFile != null && apkFile.exists()) {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.android.package-archive"
                        putExtra(Intent.EXTRA_STREAM, apkUri)
                        putExtra(Intent.EXTRA_TEXT, shareCaption)
                        putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v1.0")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (targetWhatsAppOnly) {
                            setPackage("com.whatsapp")
                        }
                    }

                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val chooser = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.android.package-archive"
                                putExtra(Intent.EXTRA_STREAM, apkUri)
                                putExtra(Intent.EXTRA_TEXT, shareCaption)
                                putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v1.0")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                            "Bagikan Berkas Installer RKCE"
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }
                } else {
                    // Fallback kirim link teks jika berkas tidak dapat diekstrak
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareCaption)
                        if (targetWhatsAppOnly) {
                            setPackage("com.whatsapp")
                        }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val chooser = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareCaption)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                            "Bagikan Tautan Unduh RKCE"
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Tidak dapat membagikan installer: ${e.message}"))
            }
        }
    }

    fun launchApkInstaller(context: Context) {
        viewModelScope.launch {
            try {
                val apkFile = getOrExtractApkFile(context)
                if (apkFile != null && apkFile.exists()) {
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(installIntent)
                } else {
                    _uiEvents.emit(UiMessage.Error("Berkas installer belum siap diekstrak."))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiMessage.Error("Gagal membuka penginstal: ${e.message}"))
            }
        }
    }

    fun refreshAnalyticsData() {
        viewModelScope.launch {
            _isSyncingMaster.value = true
            fetchMasterData()
            syncPendingReports()
            _isSyncingMaster.value = false
            _uiEvents.emit(UiMessage.Success("Data Analitik berhasil diperbarui secara real-time dari server Web Admin"))
        }
    }

    // ==========================================
    // FLEET ANALYTICS CALCULATION (ISOLASI DATA)
    // ==========================================
    fun setAnalyticsPeriod(period: String) {
        _analyticsPeriod.value = period
    }

    fun setAnalyticsUnitFilter(unit: String) {
        _analyticsUnitFilter.value = unit
    }

    private fun calculateAnalytics(
        allReports: List<ActivityReportEntity>,
        period: String,
        unitFilter: String
    ): FleetAnalyticsState {
        // Filter by Period
        val now = System.currentTimeMillis()
        val periodFiltered = allReports.filter { report ->
            when (period) {
                "30 Hari" -> (now - report.createdAt) <= (30L * 24 * 60 * 60 * 1000)
                "Bulan Ini" -> {
                    val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
                    report.reportDate.contains(currentMonth) || (now - report.createdAt) <= (31L * 24 * 60 * 60 * 1000)
                }
                "Custom Range" -> (now - report.createdAt) <= (14L * 24 * 60 * 60 * 1000) // 14 hari terakhir
                else -> true
            }
        }

        // Filter by Unit
        val filtered = if (unitFilter == "Semua Unit") {
            periodFiltered
        } else {
            periodFiltered.filter { it.unitCode.equals(unitFilter, ignoreCase = true) }
        }

        val totalSolar = filtered.sumOf { if (it.isRefueled) it.fuelLiters else 0.0 }
        val totalHm = filtered.sumOf { it.hmDailyRunning }
        val fuelRatio = if (totalHm > 0.0) totalSolar / totalHm else 0.0

        val operasiHours = filtered.filter { it.unitStatus == "OPERASI" }.sumOf { it.workDurationHours }
        val standbyHours = filtered.filter { it.unitStatus == "STANDBY" }.sumOf { it.workDurationHours }
        val breakdownHours = filtered.filter { it.unitStatus == "BREAKDOWN" }.sumOf { it.workDurationHours }
        val totalHours = operasiHours + standbyHours + breakdownHours

        val opPercent = if (totalHours > 0) (operasiHours / totalHours).toFloat() else 0.8f
        val stbPercent = if (totalHours > 0) (standbyHours / totalHours).toFloat() else 0.15f
        val bdnPercent = if (totalHours > 0) (breakdownHours / totalHours).toFloat() else 0.05f

        // Unit Summaries
        val unitsMap = filtered.groupBy { it.unitCode }
        val unitRows = unitsMap.map { (code, reps) ->
            val uTotalHours = reps.sumOf { it.workDurationHours }
            val uTotalHm = reps.sumOf { it.hmDailyRunning }
            val uSolar = reps.sumOf { if (it.isRefueled) it.fuelLiters else 0.0 }
            val uRatio = if (uTotalHm > 0.0) uSolar / uTotalHm else 0.0
            val uTroubleCount = reps.count { it.unitStatus != "OPERASI" }
            val uOutput = reps.sumOf { it.workOutput }
            val uModel = reps.firstOrNull()?.unitType ?: "HEAVY UNIT"
            val uSatuan = reps.firstOrNull()?.unitOfMeasure ?: "m3"

            UnitAnalyticsRow(
                unitCode = code,
                model = uModel,
                totalWorkHours = uTotalHours,
                totalHmRunning = uTotalHm,
                totalFuelLiters = uSolar,
                efficiencyRatio = uRatio,
                standbyBreakdownCount = uTroubleCount,
                totalOutputVolume = uOutput,
                unitOfMeasure = uSatuan
            )
        }.sortedByDescending { it.totalHmRunning }

        // Activity Summaries
        val actMap = filtered.groupBy { it.sapCode.ifBlank { it.activityName } }
        val actRows = actMap.map { (code, reps) ->
            val first = reps.first()
            val totalOutput = reps.sumOf { it.workOutput }
            val totalHmRunning = reps.sumOf { it.hmDailyRunning }
            val uniqueUnits = reps.map { it.unitCode }.distinct().size

            ActivityAnalyticsRow(
                activityName = first.activityName,
                sapCode = first.sapCode.ifBlank { "ACT" },
                unitOfMeasure = first.unitOfMeasure,
                totalOutputVolume = totalOutput,
                totalHm = totalHmRunning,
                unitInvolvedCount = uniqueUnits
            )
        }.sortedByDescending { it.totalOutputVolume }

        return FleetAnalyticsState(
            selectedPeriod = period,
            selectedUnitFilter = unitFilter,
            totalSolarLiters = totalSolar,
            totalHmRunning = totalHm,
            avgFuelConsumptionRatio = fuelRatio,
            effectiveOperationHours = operasiHours,
            standbyHours = standbyHours,
            breakdownHours = breakdownHours,
            operationPercent = opPercent,
            standbyPercent = stbPercent,
            breakdownPercent = bdnPercent,
            unitSummaries = unitRows,
            activitySummaries = actRows
        )
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
            _uiEvents.emit(UiMessage.Info("Laporan dihapus dari riwayat"))
        }
    }
}

class HeavyTrackViewModelFactory(
    private val repository: HeavyTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeavyTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeavyTrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
