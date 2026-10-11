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
import com.example.data.remote.RealtimeSocketManager
import com.example.util.ImageCompressor
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
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
        const val INTERNAL_WEB_ADMIN_URL = "https://1-portalce.ai.studio"
        const val GITHUB_REPO_URL = "https://github.com/tiaprima8-png/tes"
        const val APK_DIRECT_DOWNLOAD_URL = "https://github.com/tiaprima8-png/tes/releases/latest/download/app-release.apk"
        const val GITHUB_RELEASES_PAGE_URL = "https://github.com/tiaprima8-png/tes/releases"
        const val APK_DEBUG_DOWNLOAD_URL = "https://github.com/tiaprima8-png/tes/releases/latest/download/app-debug.apk"
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

    fun openApkDownloadInBrowser(context: Context, urlToOpen: String? = null) {
        val targetUrl = (urlToOpen ?: _customApkUrl.value).ifEmpty { APK_DIRECT_DOWNLOAD_URL }
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Gagal membuka browser: ${e.localizedMessage ?: "Tidak ada aplikasi peramban"}"))
            }
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

    // Real-time WebSocket Client via Socket.IO
    val realtimeSocketManager = RealtimeSocketManager(repository.apiClientManager.context)
    val isSocketConnected: StateFlow<Boolean> = realtimeSocketManager.isConnected

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

    private val _isBootstrapping = MutableStateFlow(false)
    val isBootstrapping: StateFlow<Boolean> = _isBootstrapping.asStateFlow()

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

    // All reports across all inputs / supervisors
    val allReports: StateFlow<List<ActivityReportEntity>> = repository.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaGlobal: StateFlow<List<RencanaKerjaEntity>> = repository.getAllRencana()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hasil Realisasi list
    val hasilAktivitasList: StateFlow<List<HasilInputAktivitasEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getHasilForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Rencana Kerja (shift today / active and NOT yet realized)
    val activeRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getActiveRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selector Unit: Status informatif 3 Kasus (Kasus A: Backlog Kemarin, Kasus B: Belum Ada Rencana Hari Ini, Kasus C: Sudah Ada Rencana Hari Ini / Tambah Susulan)
    val availableUnitsForRencana: StateFlow<List<UnitPlanStatus>> = combine(
        units,
        allRencanaList
    ) { unitList, rencanaList ->
        val nowMs = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nowMs))
        val yesterdayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nowMs - oneDayMs))
        val twoDaysAgoStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nowMs - 2 * oneDayMs))

        unitList.map { u ->
            // Rencana kerja unit untuk H-2, H-1 (Kemarin), & Hari Ini
            val plansTwoDaysAgo = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == twoDaysAgoStr
            }
            val plansYesterday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == yesterdayStr
            }
            val plansToday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == todayStr
            }

            when {
                plansTwoDaysAgo.isEmpty() -> {
                    // Kasus A1 (Backlog H-2): Wajib diisi terlebih dahulu
                    UnitPlanStatus(
                        unit = u,
                        targetDate = twoDaysAgoStr,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [H-2 ($twoDaysAgoStr): Belum Diisi]"
                    )
                }
                plansYesterday.isEmpty() -> {
                    // Kasus A2 (Backlog H-1 Kemarin): Wajib diisi
                    UnitPlanStatus(
                        unit = u,
                        targetDate = yesterdayStr,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [H-1 ($yesterdayStr): Belum Diisi]"
                    )
                }
                plansToday.isEmpty() -> {
                    // Kasus B (Belum Ada Rencana Hari Ini)
                    UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = false,
                        existingPlanCount = 0,
                        labelText = "${u.unitCode} [Hari Ini ($todayStr): Belum Ada Rencana]"
                    )
                }
                else -> {
                    // Kasus C (Sudah Ada Rencana Hari Ini): Pengawas tetap dapat menambah rencana susulan
                    val count = plansToday.size
                    UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = true,
                        existingPlanCount = count,
                        labelText = "${u.unitCode} [Hari Ini: Sudah Ada $count Rencana] (+ Tambah Rencana Susulan)"
                    )
                }
            }
        }.sortedWith(
            compareBy<UnitPlanStatus> {
                when {
                    it.isBacklog && it.targetDate == twoDaysAgoStr -> 0 // Prioritas 1: Backlog H-2 (Paling lama)
                    it.isBacklog -> 1 // Prioritas 2: Backlog H-1
                    !it.isSusulan -> 2 // Prioritas 3: Hari ini baru
                    else -> 3 // Prioritas 4: Hari ini susulan
                }
            }.thenBy { it.unit.unitCode }
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
    // 4. KARTU RINGKASAN PROGRES HARIAN (SHIFT PROGRESS TRACKER)
    // ==========================================
    val fleetShiftProgress: StateFlow<FleetShiftProgressState> = combine(
        units,
        allRencanaList,
        hasilList
    ) { unitList, rencanaList, realisasiList ->
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val plansToday = rencanaList.filter { it.tanggalFormat == todayStr }
        val resultsToday = realisasiList.filter { it.tanggalFormat == todayStr }

        // Unit yang masuk dalam perhitungan: semua unit yang beroperasi / memiliki rencana hari ini, atau seluruh unit terdaftar
        val baseUnits = if (unitList.isNotEmpty()) unitList else plansToday.map {
            UnitEntity(unitCode = it.kodeUnit, unitType = "HEAVY", brandModel = "Standard", lastHm = 0.0, supervisorAssigned = "", status = it.statusUnit)
        }.distinctBy { it.unitCode }

        val items = baseUnits.map { u ->
            val uPlans = plansToday.filter { it.kodeUnit.equals(u.unitCode, ignoreCase = true) }
            val uResults = resultsToday.filter { it.kodeUnit.equals(u.unitCode, ignoreCase = true) }
            // Unit terealisasi jika memiliki hasil realisasi hari ini atau rencana telah ditandai isRealized
            val isRealized = uResults.isNotEmpty() || uPlans.any { it.isRealized }
            val latestPlan = uPlans.maxByOrNull { it.createdAt }
            UnitShiftProgressItem(
                unitCode = u.unitCode,
                isRealized = isRealized,
                rencanaCount = uPlans.size,
                latestActivity = latestPlan?.namaAktivitas?.ifBlank { latestPlan.sapCode } ?: u.unitType,
                latestOperator = latestPlan?.operator ?: "",
                spk = latestPlan?.nomorSpk ?: ""
            )
        }

        val total = items.size
        val realized = items.count { it.isRealized }
        val pending = total - realized
        val pct = if (total > 0) (realized.toFloat() / total.toFloat()) else 0f

        FleetShiftProgressState(
            realizedUnitsCount = realized,
            totalUnitsCount = total,
            pendingUnitsCount = pending,
            progressPercentage = pct,
            isAllCompleted = total > 0 && realized == total,
            unitItems = items
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FleetShiftProgressState())

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
            repository.reconcileSyncStatus()
            fetchBootstrapData(showOverlay = false)
        }

        // 1. Hubungkan client WebSocket ke backend Web Admin
        val targetServerUrl = repository.apiClientManager.baseUrl
        realtimeSocketManager.connect(targetServerUrl)

        // 2. WEBSOCKET LISTENER REAL-TIME (SPK_PUBLISHED)
        viewModelScope.launch {
            realtimeSocketManager.spkPublishedEvents.collect { event ->
                handleSpkPublished(event.rencanaId, event.unitKode, event.nomorSpk, event.statusSpk)
            }
        }

        // 3. Listener event SPK dari backend simulator (bridge)
        viewModelScope.launch {
            CompanyServerSimulator.spkEventFlow.collect { event ->
                handleSpkPublished(event.planId, event.kodeUnit, event.nomorSpk, "SPK_TERBIT")
            }
        }
    }

    /**
     * Listener & Handler Real-Time Penerbitan SPK (SPK_PUBLISHED):
     * 1. Update database SQLite lokal / Room:
     *    rencanaKerjaDao.updateSpkStatus(unitKode, nomorSpk, statusSpk)
     * 2. Perbarui State UI secara instan (Reaktif):
     *    Ganti badge unit dari kuning "Menunggu SPK" menjadi hijau "SPK: $nomorSpk".
     * 3. Tampilkan Push Notification / In-App Toast:
     *    "SPK Diterbitkan: $nomorSpk untuk unit $unitKode"
     */
    fun handleSpkPublished(rencanaId: Long, unitKode: String, nomorSpk: String, statusSpk: String = "SPK_TERBIT") {
        viewModelScope.launch {
            // 1. Update ke SQLite lokal / Room
            if (unitKode.isNotBlank()) {
                repository.updateSpkStatus(unitKode, nomorSpk, statusSpk)
            }
            if (rencanaId > 0) {
                repository.updateRencanaSpk(rencanaId, nomorSpk, statusSpk)
            }

            // 2. Perbarui state unit yang sedang aktif dipilih jika cocok
            val cur = _realisasiFormState.value.selectedRencana
            val isCurrentUnit = cur != null && (
                (unitKode.isNotBlank() && cur.kodeUnit.equals(unitKode, ignoreCase = true)) ||
                (rencanaId > 0 && cur.id == rencanaId)
            )
            if (isCurrentUnit && cur != null) {
                _realisasiFormState.value = _realisasiFormState.value.copy(
                    nomorSpk = nomorSpk,
                    statusSpk = statusSpk,
                    selectedRencana = cur.copy(
                        nomorSpk = nomorSpk,
                        statusSpk = statusSpk
                    )
                )
            }

            // 3. Tampilkan Push Notification / In-App Toast
            NotificationHelper.showSpkPublishedNotification(
                repository.apiClientManager.context,
                unitKode = unitKode,
                nomorSpk = nomorSpk
            )
            _uiEvents.emit(
                UiMessage.Success("SPK Diterbitkan: $nomorSpk untuk unit $unitKode")
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeSocketManager.disconnect()
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
            fetchBootstrapData(showOverlay = true)
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
        fetchBootstrapData(showOverlay = true)
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
    // AUTO-SYNC & MASTER DATA / BOOTSTRAP HYDRATION
    // ==========================================
    fun fetchBootstrapData(showOverlay: Boolean = true) {
        viewModelScope.launch {
            if (showOverlay) {
                _isBootstrapping.value = true
            }
            _isSyncingMaster.value = true
            val nowMs = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(nowMs))
            val currentSupervisor = _activeSupervisor.value

            val result = repository.fetchBootstrapData(currentSupervisor, todayStr)
            _isSyncingMaster.value = false
            if (showOverlay) {
                _isBootstrapping.value = false
            }

            result.onSuccess { summary ->
                _uiEvents.emit(UiMessage.Success("Sinkronisasi Selesai: $summary"))

                // Auto-select kartu penugasan unit yang SPK-nya sudah terbit
                if (_realisasiFormState.value.selectedRencana == null) {
                    val activePlans = repository.getActiveRencanaForSupervisor(currentSupervisor).firstOrNull() ?: emptyList()
                    val targetPlan = activePlans.find { it.statusSpk == "TERBIT_SPK" && !it.isRealized }
                        ?: activePlans.firstOrNull { !it.isRealized }
                    if (targetPlan != null) {
                        selectRencanaForRealisasi(targetPlan)
                    }
                }
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Info("Mode Offline: ${err.message ?: "Data dimuat dari SQLite lokal"}"))
                if (_realisasiFormState.value.selectedRencana == null) {
                    val activePlans = repository.getActiveRencanaForSupervisor(currentSupervisor).firstOrNull() ?: emptyList()
                    val targetPlan = activePlans.find { it.statusSpk == "TERBIT_SPK" && !it.isRealized }
                        ?: activePlans.firstOrNull { !it.isRealized }
                    if (targetPlan != null) {
                        selectRencanaForRealisasi(targetPlan)
                    }
                }
            }
        }
    }

    /**
     * AKSI TOMBOL "SINKRON" (MANUAL PULL DARI SERVER)
     * Saat user menekan tombol "Sinkron" di pojok kanan atas:
     * 1. Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * 2. Ganti seluruh daftar rencana kerja di layar dengan data terbaru dari server.
     * 3. Jika unit terpilih memiliki status_spk == 'SPK_TERBIT', langsung tampilkan nomor SPK
     *    di Card "1. Nomor SPK" dan buka kunci form realisasi.
     */
    fun triggerHeaderSync() {
        viewModelScope.launch {
            _isSyncingMaster.value = true

            // Sinkronkan pending laporan terlebih dahulu jika ada
            val pendingCount = repository.getUnsyncedCountValue()
            if (pendingCount > 0) {
                _isSyncingPending.value = true
                repository.syncAllPendingOperationalData()
                _isSyncingPending.value = false
            }

            // 1. Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
            // 2. Ganti seluruh daftar rencana kerja di layar dengan data terbaru dari server
            val pullResult = repository.fetchAndReplaceActiveRencana(_activeSupervisor.value)

            pullResult.onSuccess { (newPlans, msg) ->
                // Refresh master data pendukung
                fetchBootstrapData(showOverlay = false)

                // 3. Jika unit terpilih memiliki status_spk == 'SPK_TERBIT',
                // langsung tampilkan nomor SPK di Card "1. Nomor SPK" dan buka kunci form realisasi
                val currentSelected = _realisasiFormState.value.selectedRencana
                val matchingPlan = if (currentSelected != null) {
                    newPlans.find { it.id == currentSelected.id }
                        ?: newPlans.find { it.kodeUnit.equals(currentSelected.kodeUnit, ignoreCase = true) }
                } else {
                    newPlans.firstOrNull()
                }

                if (matchingPlan != null) {
                    selectRencanaForRealisasi(matchingPlan)
                    val isSpkPublished = matchingPlan.statusSpk.equals("SPK_TERBIT", ignoreCase = true) ||
                            matchingPlan.statusSpk.equals("TERBIT_SPK", ignoreCase = true) ||
                            (matchingPlan.nomorSpk.isNotBlank() && matchingPlan.nomorSpk != "SPK Menyusul" && matchingPlan.nomorSpk != "-")

                    if (isSpkPublished) {
                        _realisasiFormState.value = _realisasiFormState.value.copy(
                            nomorSpk = matchingPlan.nomorSpk,
                            statusSpk = "SPK_TERBIT",
                            selectedRencana = matchingPlan.copy(
                                nomorSpk = matchingPlan.nomorSpk,
                                statusSpk = "SPK_TERBIT"
                            )
                        )
                    }
                }

                val spkNotice = matchingPlan?.let {
                    if (it.isSpkTerbit) " [SPK ${it.kodeUnit}: ${it.nomorSpk}]" else ""
                } ?: ""
                _uiEvents.emit(UiMessage.Success("Sinkron Berhasil: $msg$spkNotice"))
            }.onFailure { err ->
                fetchBootstrapData(showOverlay = false)
                _uiEvents.emit(UiMessage.Info("Sinkron Server: ${err.localizedMessage ?: err.message}"))
            }

            _isSyncingMaster.value = false
        }
    }

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

    // 5. AUTO-FETCH DATA MASTER SAAT HALAMAN DIBUKA (ONRESUME / ONENTER)
    fun autoFetchMasterDataOnResume() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Non-blocking background sync: updates master kendala & unit standards (hm_min_standar / hm_max_standar)
                repository.fetchMasterData(_activeSupervisor.value)

                // If a unit is currently selected in form, refresh dynamic standards from the latest DB
                val currentSelectedUnitCode = _realisasiFormState.value.selectedRencana?.kodeUnit
                if (!currentSelectedUnitCode.isNullOrBlank()) {
                    val updatedDbUnit = repository.getUnitByCode(currentSelectedUnitCode)
                    if (updatedDbUnit != null) {
                        val state = _realisasiFormState.value
                        val cleanAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                        val (isValid, running, errorMsg) = validateHm(state.hmAwal, cleanAkhir)
                        _realisasiFormState.value = state.copy(
                            hmMinStandar = updatedDbUnit.hmMinStandar,
                            hmMaxStandar = updatedDbUnit.hmMaxStandar,
                            isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < updatedDbUnit.hmMinStandar
                        )
                    }
                }
            } catch (_: Exception) {
                // Fallback non-blocking: data lokal SQLite tetap aktif
            }
        }
    }

    fun syncPendingReports() {
        viewModelScope.launch {
            val pendingCount = repository.getUnsyncedCountValue()
            if (pendingCount <= 0) {
                _uiEvents.emit(UiMessage.Info("Semua laporan sudah tersinkron ke Web Admin."))
                return@launch
            }
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
        val current = _rencanaFormState.value
        val isDifferentUnit = current.selectedUnit?.unitCode != status.unit.unitCode
        _rencanaFormState.value = current.copy(
            selectedUnitStatus = status,
            selectedUnit = status.unit,
            targetDate = status.targetDate,
            isBacklog = status.isBacklog,
            isSusulan = status.isSusulan,
            // Jika memilih unit baru atau unit Kasus C (susulan), persiapkan form input bersih
            selectedOperator = if (isDifferentUnit) null else current.selectedOperator,
            selectedActivity = if (isDifferentUnit) null else current.selectedActivity,
            selectedLokasi = if (isDifferentUnit) null else current.selectedLokasi,
            keterangan = if (isDifferentUnit) "" else current.keterangan,
            subPlans = if (isDifferentUnit) emptyList() else current.subPlans
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
                isBacklog = false,
                isSusulan = false
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
                val planNotice = when {
                    state.isBacklog -> " [Backlog Tertinggal Terselesaikan]"
                    state.isSusulan -> " [Rencana Kerja Susulan Ditambahkan]"
                    else -> ""
                }
                val planType = if (state.isSusulan) "Rencana Kerja Susulan" else "Rencana Kerja"
                _uiEvents.emit(
                    UiMessage.Success("Berhasil membuat $totalPlans $planType untuk Unit ${saved.kodeUnit} ($targetDate)$planNotice")
                )
                // Reset form
                _rencanaFormState.value = RencanaFormUiState(
                    shiftKerja = state.shiftKerja
                )
                // Otomatis beralih ke Tahap 2 Realisasi Kerja dengan rencana baru yang dibuat
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

        // 2. KESINAMBUNGAN HM PADA MULTI-RENCANA DALAM 1 UNIT (SEQUENTIAL HM)
        // Cek riwayat realisasi in-memory pada unit yang sama di shift/sesi ini
        val memoryLatestHasil = hasilAktivitasList.value
            .filter { it.kodeUnit.equals(rencana.kodeUnit, ignoreCase = true) }
            .maxByOrNull { it.createdAt }
        val isSeqMem = memoryLatestHasil != null && memoryLatestHasil.hmAkhir > 0.0
        val sequentialAwalMem = if (isSeqMem) memoryLatestHasil!!.hmAkhir else null
        val hmAwalVal = sequentialAwalMem ?: unitEntity?.lastHm ?: 0.0
        val sequentialTextMem = if (isSeqMem) "HM Akhir Rencana Sebelumnya: ${String.format(Locale.US, "%.1f", hmAwalVal)}" else ""

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

        val resolvedSpk = if (rencana.nomorSpk.isNotBlank() && rencana.nomorSpk != "Belum Terbit (Menunggu Admin)") rencana.nomorSpk else ""

        _realisasiFormState.value = _realisasiFormState.value.copy(
            selectedRencana = rencana,
            selectedActivity = matchedActivity ?: _realisasiFormState.value.selectedActivity,
            selectedLokasi = matchedLokasi ?: _realisasiFormState.value.selectedLokasi,
            nomorSpk = resolvedSpk,
            statusSpk = rencana.statusSpk,
            hmAwalOriginal = hmAwalVal,
            hmAwal = hmAwalVal,
            hmAwalInput = String.format(Locale.US, "%.1f", hmAwalVal),
            isHmAwalCorrected = false,
            alasanKoreksiHm = "",
            isSequentialHmFromPreviousPlan = isSeqMem,
            sequentialSourcePlanText = sequentialTextMem,
            clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString(),
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

        // Query database lokal secara asinkron untuk memastikan kesinambungan HM berantai dan auto-restore draft
        viewModelScope.launch {
            val dbUnit = repository.getUnitByCode(rencana.kodeUnit)
            val latestRealizedHm = repository.getLatestRealizedHmForUnit(rencana.kodeUnit)
            val isSeqDb = latestRealizedHm != null && latestRealizedHm > 0.0 && (dbUnit == null || latestRealizedHm >= dbUnit.lastHm)
            val resolvedAwal = latestRealizedHm ?: dbUnit?.lastHm ?: hmAwalVal
            val seqTextDb = if (isSeqDb) "HM Akhir Rencana Sebelumnya: ${String.format(Locale.US, "%.1f", resolvedAwal)}" else ""

            val curState = _realisasiFormState.value
            val effectiveMinStd = dbUnit?.hmMinStandar ?: curState.hmMinStandar
            val effectiveMaxStd = dbUnit?.hmMaxStandar ?: curState.hmMaxStandar

            // Cek apakah terdapat Auto-Save Draft di database lokal untuk rencana/unit ini
            val savedDraft = repository.getDraftForRencana(rencana.id)
            if (savedDraft != null) {
                val restoredAwal = if (savedDraft.isHmAwalCorrected) {
                    savedDraft.hmAwalInput.replace(',', '.').toDoubleOrNull() ?: resolvedAwal
                } else resolvedAwal

                val cleanAkhir = savedDraft.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                val (isValid, running, errorMsg) = validateHm(restoredAwal, cleanAkhir)

                val restoredKendala = if (savedDraft.daftarKendalaJson.isNotBlank()) {
                    val payloads = KendalaJsonConverter.fromJson(savedDraft.daftarKendalaJson)
                    if (payloads.isNotEmpty()) {
                        payloads.map { p ->
                            val kndCode = p.id_kendala ?: ""
                            val kndName = p.nama_kendala ?: ""
                            val matched = kendalaList.value.find { it.kodeKendala.equals(kndCode, ignoreCase = true) }
                            KendalaItemFormState(
                                selectedKendala = matched ?: KendalaEntity(
                                    kodeKendala = kndCode,
                                    namaKendala = kndName,
                                    kategori = "OPERASIONAL"
                                ),
                                waktuMulai = p.waktu_mulai ?: "",
                                waktuSelesai = p.waktu_selesai ?: ""
                            )
                        }
                    } else listOf(KendalaItemFormState())
                } else listOf(KendalaItemFormState())

                _realisasiFormState.value = curState.copy(
                    hmMinStandar = effectiveMinStd,
                    hmMaxStandar = effectiveMaxStd,
                    hmAwalOriginal = resolvedAwal,
                    hmAwal = restoredAwal,
                    hmAwalInput = savedDraft.hmAwalInput.ifBlank { String.format(Locale.US, "%.1f", restoredAwal) },
                    isHmAwalCorrected = savedDraft.isHmAwalCorrected,
                    alasanKoreksiHm = savedDraft.alasanKoreksiHm,
                    hmAkhirInput = savedDraft.hmAkhirInput,
                    stikSolarAwalInput = savedDraft.stikSolarAwalInput,
                    stikSolarAkhirInput = savedDraft.stikSolarAkhirInput,
                    isIsiSolar = savedDraft.isIsiSolar,
                    jumlahLiterSolarInput = savedDraft.jumlahLiterSolarInput,
                    daftarKendala = restoredKendala,
                    selectedKendala = restoredKendala.firstOrNull()?.selectedKendala,
                    hasilKerjaInput = savedDraft.hasilKerjaInput,
                    keterangan = savedDraft.keterangan,
                    photoPath = savedDraft.photoPath,
                    photoBase64 = savedDraft.photoBase64,
                    photoSizeKb = savedDraft.photoSizeKb,
                    photoDimensions = savedDraft.photoDimensions,
                    clientTransactionId = savedDraft.clientTransactionId.ifBlank { curState.clientTransactionId },
                    isSequentialHmFromPreviousPlan = isSeqDb || curState.isSequentialHmFromPreviousPlan,
                    sequentialSourcePlanText = if (seqTextDb.isNotBlank()) seqTextDb else curState.sequentialSourcePlanText,
                    isHmValid = isValid,
                    hmHarianBerjalan = running,
                    hmErrorMessage = errorMsg,
                    isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < effectiveMinStd
                )
                recalculateSolarConsumption()
                _uiEvents.emit(UiMessage.Info("Draft inputan formulir ${rencana.kodeUnit} berhasil dipulihkan otomatis."))
            } else {
                val updatedAwal = if (!curState.isHmAwalCorrected) resolvedAwal else curState.hmAwal
                val cleanAkhir = curState.hmAkhirInput.replace(',', '.').toDoubleOrNull()
                val (isValid, running, errorMsg) = validateHm(updatedAwal, cleanAkhir)

                _realisasiFormState.value = curState.copy(
                    hmMinStandar = effectiveMinStd,
                    hmMaxStandar = effectiveMaxStd,
                    hmAwalOriginal = resolvedAwal,
                    hmAwal = updatedAwal,
                    hmAwalInput = if (!curState.isHmAwalCorrected) String.format(Locale.US, "%.1f", resolvedAwal) else curState.hmAwalInput,
                    isSequentialHmFromPreviousPlan = isSeqDb || curState.isSequentialHmFromPreviousPlan,
                    sequentialSourcePlanText = if (seqTextDb.isNotBlank()) seqTextDb else curState.sequentialSourcePlanText,
                    isHmValid = isValid,
                    hmHarianBerjalan = running,
                    hmErrorMessage = errorMsg,
                    isUnderMinStandard = cleanAkhir != null && isValid && running > 0.0 && running < effectiveMinStd
                )
            }
        }
    }

    // Fitur Koreksi Manual HM Awal & Logika Pergantian Shift
    fun toggleKoreksiHmAwal() {
        val current = _realisasiFormState.value
        val newCorrected = !current.isHmAwalCorrected
        if (newCorrected) {
            _realisasiFormState.value = current.copy(
                isHmAwalCorrected = true,
                hmAwalInput = if (current.hmAwal > 0.0) String.format(Locale.US, "%.1f", current.hmAwal) else ""
            )
        } else {
            // Kembalikan ke HM Awal riwayat sistem default
            val original = current.hmAwalOriginal
            val cleanHmAkhir = current.hmAkhirInput.replace(',', '.').toDoubleOrNull()
            val (isValid, running, errorMsg) = validateHm(original, cleanHmAkhir)
            val isUnderMin = cleanHmAkhir != null && isValid && running > 0.0 && running < current.hmMinStandar
            val kendalaList = if (isUnderMin && current.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else current.daftarKendala

            _realisasiFormState.value = current.copy(
                isHmAwalCorrected = false,
                hmAwal = original,
                hmAwalInput = String.format(Locale.US, "%.1f", original),
                alasanKoreksiHm = "",
                isHmValid = isValid,
                hmHarianBerjalan = running,
                hmErrorMessage = errorMsg,
                isUnderMinStandard = isUnderMin,
                daftarKendala = kendalaList
            )
        }
    }

    fun setRealisasiHmAwalManual(input: String) {
        val state = _realisasiFormState.value
        val cleanInput = input.replace(',', '.')
        val parsedAwal = cleanInput.toDoubleOrNull() ?: 0.0
        val cleanHmAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
        val (isValid, running, errorMsg) = validateHm(parsedAwal, cleanHmAkhir)
        val isUnderMin = cleanHmAkhir != null && isValid && running > 0.0 && running < state.hmMinStandar
        val kendalaList = if (isUnderMin && state.daftarKendala.isEmpty()) listOf(KendalaItemFormState()) else state.daftarKendala

        _realisasiFormState.value = state.copy(
            hmAwalInput = cleanInput,
            hmAwal = parsedAwal,
            isHmValid = isValid,
            hmHarianBerjalan = running,
            hmErrorMessage = errorMsg,
            isUnderMinStandard = isUnderMin,
            daftarKendala = kendalaList
        )
        recalculateSolarConsumption()
    }

    fun setRealisasiAlasanKoreksiHm(reason: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(alasanKoreksiHm = reason)
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
        autoSaveDraftCurrentRealisasi()
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
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiStikAkhir(input: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            stikSolarAkhirInput = input.replace(',', '.')
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiIsiSolar(isIsi: Boolean) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            isIsiSolar = isIsi,
            jumlahLiterSolarInput = if (!isIsi) "" else _realisasiFormState.value.jumlahLiterSolarInput
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiJumlahSolar(liters: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            jumlahLiterSolarInput = liters.replace(',', '.')
        )
        recalculateSolarConsumption()
        autoSaveDraftCurrentRealisasi()
    }

    private fun recalculateSolarConsumption() {
        val state = _realisasiFormState.value
        val awal = state.parsedStikAwal ?: 0.0
        val akhir = state.parsedStikAkhir ?: 0.0
        val isi = if (state.isIsiSolar) (state.parsedJumlahLiterSolar ?: 0.0) else 0.0
        // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
        val total = (awal + isi) - akhir
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
        autoSaveDraftCurrentRealisasi()
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
            autoSaveDraftCurrentRealisasi()
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
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun updateKendalaWaktuMulai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuMulai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun updateKendalaWaktuSelesai(index: Int, time: String) {
        val current = _realisasiFormState.value.daftarKendala.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(waktuSelesai = time)
            _realisasiFormState.value = _realisasiFormState.value.copy(daftarKendala = current)
            autoSaveDraftCurrentRealisasi()
        }
    }

    fun setRealisasiCatatanKendala(notes: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(catatanKendala = notes)
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiHasilKerja(hasil: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(hasilKerjaInput = hasil)
        autoSaveDraftCurrentRealisasi()
    }

    fun setRealisasiKeterangan(ket: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(keterangan = ket)
        autoSaveDraftCurrentRealisasi()
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
                autoSaveDraftCurrentRealisasi()
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
        autoSaveDraftCurrentRealisasi()
    }

    // ======================================================================
    // 1. AUTO-SAVE DRAFT FORMULIR REALISASI (ANTI-HILANG DATA)
    // ======================================================================
    fun autoSaveDraftCurrentRealisasi() {
        val state = _realisasiFormState.value
        val ren = state.selectedRencana ?: return
        viewModelScope.launch {
            val kendalaPayloads = state.daftarKendala
                .filter { it.selectedKendala != null || it.waktuMulai.isNotBlank() || it.waktuSelesai.isNotBlank() }
                .map { item ->
                    KendalaItemPayload(
                        id_kendala = item.selectedKendala?.kodeKendala ?: "",
                        nama_kendala = item.selectedKendala?.namaKendala ?: "",
                        waktu_mulai = item.waktuMulai,
                        waktu_selesai = item.waktuSelesai,
                        durasi_menit = item.durasiMenit ?: 0
                    )
                }
            val jsonKendala = if (kendalaPayloads.isNotEmpty()) KendalaJsonConverter.toJson(kendalaPayloads) else ""

            val draft = com.example.data.local.entity.DraftRealisasiEntity(
                rencanaId = ren.id,
                kodeUnit = ren.kodeUnit,
                hmAwalInput = state.hmAwalInput,
                isHmAwalCorrected = state.isHmAwalCorrected,
                alasanKoreksiHm = state.alasanKoreksiHm,
                hmAkhirInput = state.hmAkhirInput,
                stikSolarAwalInput = state.stikSolarAwalInput,
                stikSolarAkhirInput = state.stikSolarAkhirInput,
                isIsiSolar = state.isIsiSolar,
                jumlahLiterSolarInput = state.jumlahLiterSolarInput,
                daftarKendalaJson = jsonKendala,
                hasilKerjaInput = state.hasilKerjaInput,
                keterangan = state.keterangan,
                photoPath = state.photoPath,
                photoBase64 = state.photoBase64,
                photoSizeKb = state.photoSizeKb,
                photoDimensions = state.photoDimensions,
                clientTransactionId = state.clientTransactionId,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDraftForRencana(draft)
        }
    }

    fun resetRealisasiFormManual() {
        val currentRencana = _realisasiFormState.value.selectedRencana
        if (currentRencana != null) {
            viewModelScope.launch {
                repository.deleteDraftForRencana(currentRencana.id)
                // Inisialisasi ulang form untuk rencana terpilih
                selectRencanaForRealisasi(currentRencana)
                _uiEvents.emit(UiMessage.Info("Formulir unit ${currentRencana.kodeUnit} berhasil direset."))
            }
        } else {
            _realisasiFormState.value = RealisasiFormUiState(
                clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
            )
        }
    }

    fun submitRealisasiKerja() {
        val state = _realisasiFormState.value
        // Kunci tombol kirim setelah 1x klik (loading state & anti-duplikasi)
        if (state.isSubmitting) return

        val rencana = state.selectedRencana
        if (rencana == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Rencana Kerja aktif terlebih dahulu")) }
            return
        }

        val hmAkhir = state.hmAkhirInput.replace(',', '.').toDoubleOrNull()
        if (hmAkhir == null || hmAkhir <= state.hmAwal) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("HM Akhir wajib diisi dan lebih besar dari HM Awal (${String.format(Locale.US, "%.1f", state.hmAwal)})"))
            }
            return
        }

        // Validasi Alasan Koreksi HM Awal jika diedit manual
        if (state.isHmAwalCorrected && state.alasanKoreksiHm.isBlank()) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Alasan koreksi HM Awal wajib diisi jika koreksi manual HM Awal diaktifkan."))
            }
            return
        }

        // Validasi Tumpang Tindih (Anti-Overlapping) Jam Kendala
        if (state.hasAnyKendalaOverlap) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Jam kendala bertabrakan dengan kendala lainnya! Perbaiki jam input kendala."))
            }
            return
        }

        // Validasi Batas Maksimal Shift untuk Kendala
        if (state.isTotalKendalaExceedingShift) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Akumulasi total durasi kendala (${state.totalHambatanLabel}) melebihi durasi jam kerja shift (${String.format(Locale.US, "%.1f", state.maxShiftHours)} Jam)!"))
            }
            return
        }

        // 1. Validasi Pemakaian Solar BBM
        if (state.isRefuelLitersMissing) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Pengisian BBM tambahan aktif: Wajib mengisi Jumlah Liter Pengisian BBM (> 0)"))
            }
            return
        }

        if (state.isSolarConsumptionNegative) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Konsumsi solar tidak boleh bernilai negatif (${String.format(Locale.US, "%.1f", state.computedKonsumsiSolar)} L). Periksa kembali stik solar dan pengisian BBM."))
            }
            return
        }

        val runningHm = hmAkhir - state.hmAwal
        val isUnderMin = runningHm > 0.0 && runningHm < state.hmMinStandar

        // Validasi HM Kurang dari Standar Minimal:
        // Wajib minimal 1 kendala lengkap dengan waktu mulai & selesai
        if (isUnderMin) {
            if (state.daftarKendala.isEmpty() || !state.daftarKendala.all { it.isValid }) {
                viewModelScope.launch {
                    val invalidReason = when {
                        state.daftarKendala.isEmpty() -> "Wajib mencatat minimal 1 kendala lapangan."
                        state.daftarKendala.any { it.selectedKendala == null } -> "Penyebab kendala wajib dipilih dari Master Kendala."
                        state.daftarKendala.any { it.waktuMulai.isBlank() || it.waktuSelesai.isBlank() } -> "Waktu mulai dan selesai kendala wajib diisi (JJ:MM)."
                        state.daftarKendala.any { !it.isTimeRangeValid } -> "Rentang jam kendala tidak valid (JJ:MM)."
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
            val parsed = state.parsedJumlahLiterSolar
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

        val stikAwal = state.parsedStikAwal ?: 0.0
        val stikAkhir = state.parsedStikAkhir ?: 0.0

        val validKendalaList: List<KendalaItemFormState> = state.daftarKendala.filter { it.isValid }

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

        // Kunci tombol kirim secara langsung
        _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = true)

        viewModelScope.launch {
            val txId = state.clientTransactionId.ifBlank { "TX-" + java.util.UUID.randomUUID().toString() }

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
                pemakaianSolar = state.computedKonsumsiSolar,
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
                tanggalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                isHmAwalCorrected = state.isHmAwalCorrected,
                alasanKoreksiHm = if (state.isHmAwalCorrected) state.alasanKoreksiHm.trim().ifBlank { "Koreksi manual HM Awal" } else null,
                clientTransactionId = txId,
                statusKirim = "PENDING"
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

                // Bersihkan draft lokal jika berhasil dikirim ke server (status SYNCED)
                if (saved.statusKirim == "SYNCED") {
                    repository.deleteDraftForRencana(saved.rencanaId)
                }

                // Reset realisasi form dengan Client Transaction ID baru
                _realisasiFormState.value = RealisasiFormUiState(
                    clientTransactionId = "TX-" + java.util.UUID.randomUUID().toString()
                )
            }.onFailure { err ->
                _uiEvents.emit(UiMessage.Error("Gagal menyimpan realisasi: ${err.message}"))
            }
        }
    }

    private fun validateHm(hmAwal: Double, hmAkhir: Double?): Triple<Boolean, Double, String?> {
        if (hmAkhir == null) {
            return Triple(true, 0.0, null)
        }
        return if (hmAkhir <= hmAwal) {
            val diff = hmAwal - hmAkhir
            val msg = if (hmAkhir == hmAwal) {
                "HM Akhir (${String.format(Locale.US, "%.1f", hmAkhir)}) harus lebih besar dari HM Awal (${String.format(Locale.US, "%.1f", hmAwal)})"
            } else {
                "HM Akhir (${String.format(Locale.US, "%.1f", hmAkhir)}) lebih kecil dari HM Awal (${String.format(Locale.US, "%.1f", hmAwal)}) selisih -${String.format(Locale.US, "%.1f", diff)}"
            }
            Triple(
                false,
                0.0,
                msg
            )
        } else {
            val running = hmAkhir - hmAwal
            if (running > 24.0) {
                Triple(
                    false,
                    running,
                    "Lonjakan HM melebihi 24 jam dalam 1 shift! Periksa kembali angka meteran HM Akhir."
                )
            } else {
                Triple(true, running, null)
            }
        }
    }

    // ==========================================
    // MODUL DISTRIBUSI & PEMASANGAN INSTALLER APK
    // ==========================================
    fun getAppVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.2"
        } catch (_: Exception) {
            "1.2"
        }
    }

    fun getOrExtractApkFile(context: Context): File? {
        return try {
            val sourcePath = context.applicationInfo.sourceDir
            if (sourcePath.isNullOrEmpty()) return null
            val sourceApk = File(sourcePath)
            if (!sourceApk.exists() || sourceApk.length() == 0L) return null

            val vName = getAppVersionName(context)
            val targetFileName = "RKCE_Installer_v${vName}.apk"

            val cacheShareDir = File(context.cacheDir, "apk_share").apply { mkdirs() }
            val targetApk = File(cacheShareDir, targetFileName)

            // Bersihkan berkas usang jika versi/timestamp berubah
            if (!targetApk.exists() || targetApk.length() != sourceApk.length() || targetApk.lastModified() < sourceApk.lastModified()) {
                cacheShareDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk")) {
                        file.delete()
                    }
                }
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
                val vName = getAppVersionName(context)
                val fileName = "RKCE_Installer_v${vName}.apk"
                var savedLocally = false
                val sourcePath = context.applicationInfo.sourceDir
                val sourceApk = if (!sourcePath.isNullOrEmpty()) File(sourcePath) else null

                if (sourceApk != null && sourceApk.exists() && sourceApk.length() > 0L) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
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
                        val destFile = File(downloadsDir, fileName)
                        sourceApk.copyTo(destFile, overwrite = true)
                        savedLocally = true
                    }
                }

                if (savedLocally) {
                    _uiEvents.emit(
                        UiMessage.Success(
                            "Installer pembaruan '$fileName' berhasil disimpan di folder Download! Berkas versi terbaru utuh dan siap dipasang atau dikirim ke HP lain."
                        )
                    )
                } else {
                    // Fallback unduh via DownloadManager dari URL resmi
                    val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                    val request = DownloadManager.Request(Uri.parse(downloadUrl))
                        .setTitle(fileName)
                        .setDescription("Mengunduh installer resmi RKCE pembaruan aplikasi...")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    dm?.enqueue(request)
                    _uiEvents.emit(UiMessage.Success("Memulai unduhan berkas APK pembaruan resmi ke folder Download perangkat..."))
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
                val vName = getAppVersionName(context)
                val fileName = "RKCE_Installer_v${vName}.apk"
                val apkFile = getOrExtractApkFile(context)
                val downloadUrl = _customApkUrl.value.ifEmpty { APK_DIRECT_DOWNLOAD_URL }
                val shareCaption = """
*Aplikasi RKCE (Rencana Kerja Civil Engineering) - Pembaruan v$vName*

Halo Rekan Pengawas, berkas installer APK pembaruan aplikasi resmi terlampir ($fileName). Anda dapat langsung memasang atau memperbarui aplikasi ini di HP Android Anda.

*PANDUAN PEMBARUAN & AGAR BISA DIINSTAL DI HP LAIN:*
1. Buka atau klik berkas *$fileName* yang terlampir.
2. Jika muncul peringatan keamanan sistem Android, pilih *'Setelan' / 'Pengaturan'* lalu aktifkan *'Izinkan dari sumber ini'*.
3. Jika muncul notifikasi Google Play Protect ('Aplikasi tidak dikenal' / 'Blocked by Play Protect'):
   👉 Klik *'Detail Lainnya'* (More details)
   👉 Pilih *'Tetap Instal'* (Install anyway).
4. *PENTING SAAT PEMBARUAN APLIKASI:*
   Jika sebelumnya sudah ada versi RKCE yang terpasang di HP dan muncul pesan 'Aplikasi tidak terpasang' (App not installed / bentrok versi lama), silakan copot (uninstall) dulu versi lama tersebut, lalu pasang berkas pembaruan ini.

*Tautan Unduh Resmi Pembaruan:*
- Unduh Langsung APK: $downloadUrl
- Halaman Rilis GitHub: $GITHUB_RELEASES_PAGE_URL
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
                        putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v$vName")
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
                                putExtra(Intent.EXTRA_SUBJECT, "Installer RKCE Mobile v$vName")
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
