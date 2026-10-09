package com.example.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.model.Supervisor
import com.example.data.model.SupervisorDirectory
import com.example.data.remote.ActivityPayload
import com.example.data.remote.CompanyServerSimulator
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
        const val APK_DIRECT_DOWNLOAD_URL = "https://1-portalce.ai.studio/downloads/HeavyTrack.apk"
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

    // Reports list (historical)
    val reports: StateFlow<List<ActivityReportEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getReportsForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Rencana Kerja (shift today / active)
    val activeRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getActiveRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRencanaList: StateFlow<List<RencanaKerjaEntity>> = _activeSupervisor
        .flatMapLatest { supervisor -> repository.getRencanaForSupervisor(supervisor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun selectRencanaUnit(unit: UnitEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedUnit = unit)
    }

    fun selectRencanaOperator(operator: OperatorEntity) {
        _rencanaFormState.value = _rencanaFormState.value.copy(selectedOperator = operator)
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

    fun submitRencanaKerja() {
        val state = _rencanaFormState.value
        val unit = state.selectedUnit
        val op = state.selectedOperator
        val lok = state.selectedLokasi

        if (unit == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Unit Alat Berat terlebih dahulu")) }
            return
        }
        if (op == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Operator Pelaksana terlebih dahulu")) }
            return
        }
        if (lok == null) {
            viewModelScope.launch { _uiEvents.emit(UiMessage.Error("Pilih Lokasi Kerja Lapangan terlebih dahulu")) }
            return
        }

        viewModelScope.launch {
            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = true)

            val newRencana = RencanaKerjaEntity(
                supervisorName = _activeSupervisor.value,
                statusUnit = state.statusUnit,
                kodeUnit = unit.unitCode,
                operator = op.name,
                kodeLokasi = lok.kodeLokasi,
                shiftKerja = state.shiftKerja,
                keterangan = state.keterangan,
                nomorSpk = "", // Admin belum tentu input SPK di awal shift
                statusSpk = "MENUNGGU_SPK",
                createdAt = System.currentTimeMillis(),
                tanggalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )

            val result = repository.submitRencanaKerja(newRencana)
            _rencanaFormState.value = _rencanaFormState.value.copy(isSubmitting = false)

            result.onSuccess { saved ->
                _uiEvents.emit(UiMessage.Success("Rencana Kerja Unit ${saved.kodeUnit} berhasil dibuat (Status: ${saved.statusSpk})"))
                // Reset form
                _rencanaFormState.value = RencanaFormUiState(
                    shiftKerja = state.shiftKerja
                )
                // Otomatis beralih ke Tahap 2 Realisasi Kerja dengan rencana yang baru dibuat
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

        _realisasiFormState.value = _realisasiFormState.value.copy(
            selectedRencana = rencana,
            nomorSpk = rencana.nomorSpk, // auto-fill jika ada SPK dari server
            hmAwal = hmAwalVal,
            hmAkhirInput = "",
            hmHarianBerjalan = 0.0,
            isHmValid = true,
            hmErrorMessage = null
        )
    }

    fun setRealisasiActivity(activity: ActivityMasterEntity) {
        _realisasiFormState.value = _realisasiFormState.value.copy(selectedActivity = activity)
    }

    fun setRealisasiNomorSpk(spk: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(nomorSpk = spk)
    }

    fun setRealisasiDurasiJam(durasi: Double) {
        _realisasiFormState.value = _realisasiFormState.value.copy(durasiJamKerja = durasi)
    }

    fun setRealisasiHmAkhir(input: String) {
        val hmAwal = _realisasiFormState.value.hmAwal
        val cleanInput = input.replace(',', '.')
        val inputDouble = cleanInput.toDoubleOrNull()

        val (isValid, running, errorMsg) = validateHm(hmAwal, inputDouble)

        _realisasiFormState.value = _realisasiFormState.value.copy(
            hmAkhirInput = cleanInput,
            isHmValid = isValid,
            hmHarianBerjalan = running,
            hmErrorMessage = errorMsg
        )
    }

    fun quickAddRealisasiHours(hours: Double) {
        val hmAwal = _realisasiFormState.value.hmAwal
        val newHmAkhir = hmAwal + hours
        setRealisasiHmAkhir(String.format(Locale.US, "%.1f", newHmAkhir))
    }

    fun setRealisasiIsiSolar(isIsi: Boolean) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            isIsiSolar = isIsi,
            jumlahLiterSolarInput = if (!isIsi) "" else _realisasiFormState.value.jumlahLiterSolarInput
        )
    }

    fun setRealisasiJumlahSolar(liters: String) {
        _realisasiFormState.value = _realisasiFormState.value.copy(
            jumlahLiterSolarInput = liters.replace(',', '.')
        )
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
            0.0 // Standby/Breakdown dinonaktifkan/0
        }

        val actName = state.selectedActivity?.activityName ?: when (rencana.statusUnit) {
            "STANDBY" -> "Standby Lapangan"
            "BREAKDOWN" -> "Breakdown / Perbaikan Unit"
            else -> "Aktivitas Operasi Lapangan"
        }
        val sapCode = state.selectedActivity?.sapCode ?: "ACT-OPR-01"
        val satuan = state.selectedActivity?.unitOfMeasure ?: "m3"

        viewModelScope.launch {
            _realisasiFormState.value = _realisasiFormState.value.copy(isSubmitting = true)

            val hasilEntity = HasilInputAktivitasEntity(
                rencanaId = rencana.id,
                nomorSpk = state.nomorSpk,
                supervisorName = _activeSupervisor.value,
                kodeUnit = rencana.kodeUnit,
                operator = rencana.operator,
                kodeLokasi = rencana.kodeLokasi,
                activityName = actName,
                sapCode = sapCode,
                satuan = satuan,
                unitStatus = rencana.statusUnit,
                shift = rencana.shiftKerja,
                jamKerja = state.durasiJamKerja,
                hmAwal = state.hmAwal,
                hmAkhir = hmAkhir,
                hmHarianBerjalan = hmAkhir - state.hmAwal,
                isIsiSolar = state.isIsiSolar,
                jumlahLiterSolar = fuelLiters,
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
                _uiEvents.emit(UiMessage.Success("Realisasi Unit ${saved.kodeUnit}$spkTag$fuelTag berhasil dikirim & disimpan!"))

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
    // MODUL DISTRIBUSI (DOWNLOAD & SHARE WHATSAPP)
    // ==========================================
    fun downloadApk(context: Context) {
        try {
            val request = DownloadManager.Request(Uri.parse(APK_DIRECT_DOWNLOAD_URL))
                .setTitle("HeavyTrack.apk")
                .setDescription("Mengunduh installer HeavyTrack Mobile...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "HeavyTrack.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            dm?.enqueue(request)
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Success("Mengunduh HeavyTrack.apk ke folder Download perangkat..."))
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage.Error("Gagal mengunduh: ${e.localizedMessage ?: "Periksa izin penyimpanan"}"))
            }
        }
    }

    fun shareApkViaWhatsApp(context: Context) {
        val shareMessage = "Halo, silakan unduh dan pasang aplikasi HeavyTrack Mobile (Sistem Pelaporan Alat Berat) melalui tautan berikut: $APK_DIRECT_DOWNLOAD_URL"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback ke general share sheet jika WhatsApp tidak terpasang
            try {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                    },
                    "Bagikan Aplikasi HeavyTrack Mobile"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage.Error("Tidak dapat membuka aplikasi berbagi: ${e.message}"))
                }
            }
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
