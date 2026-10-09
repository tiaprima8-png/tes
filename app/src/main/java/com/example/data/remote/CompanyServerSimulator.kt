package com.example.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class SpkPublishedEvent(
    val planId: Long,
    val kodeUnit: String,
    val nomorSpk: String,
    val supervisorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Simulates company central backend and Web Admin database.
 * Supports:
 * - GET /api/master-data (filtered by supervisor)
 * - POST /api/aktivitas-unit
 * - Admin web updates (tambah, edit, hapus master data)
 * - Offline simulation toggle
 * - WebSocket / Polling simulation for SPK_PUBLISHED
 */
object CompanyServerSimulator {
    var isSimulatedOffline: Boolean = false

    private val _spkEventFlow = MutableSharedFlow<SpkPublishedEvent>(extraBufferCapacity = 64)
    val spkEventFlow = _spkEventFlow.asSharedFlow()

    // Map of planId -> published SPK
    val publishedSpkMap = ConcurrentHashMap<Long, String>()

    // Central master units in company web admin
    val serverUnits = CopyOnWriteArrayList<UnitPayload>().apply {
        addAll(
            listOf(
                UnitPayload("HEH1", "EXCAVATOR", "Komatsu PC200-8", 4125.5, "Budi Santoso", "OPERASIONAL"),
                UnitPayload("EXC-02", "EXCAVATOR", "Caterpillar 320D", 6280.0, "Budi Santoso", "OPERASIONAL"),
                UnitPayload("DT-101", "DUMP TRUCK", "Hino FM 260 Ti", 3450.2, "Budi Santoso", "OPERASIONAL"),
                UnitPayload("DT-102", "DUMP TRUCK", "Scania P360", 2890.4, "Budi Santoso", "OPERASIONAL"),
                UnitPayload("DZ-01", "BULLDOZER", "Komatsu D85ESS-2", 5130.8, "Budi Santoso", "OPERASIONAL"),
                UnitPayload("GRD-01", "MOTOR GRADER", "Caterpillar 140K", 1870.0, "Budi Santoso", "OPERASIONAL"),
                // Units assigned to other supervisors
                UnitPayload("EXC-09", "EXCAVATOR", "Hitachi ZX210-5G", 3990.2, "Agus Wijaya", "OPERASIONAL"),
                UnitPayload("DT-205", "DUMP TRUCK", "Volvo FMX 440", 4150.0, "Agus Wijaya", "OPERASIONAL"),
                UnitPayload("DZ-04", "BULLDOZER", "CAT D6R", 2980.5, "Rudi Hartono", "OPERASIONAL")
            )
        )
    }

    // Central master activities in company web admin
    val serverActivities = CopyOnWriteArrayList<ActivityPayload>().apply {
        addAll(
            listOf(
                ActivityPayload("ACT-SAP-101", "Galian Tanah Lunak", "m3", "Penggalian area cut site A", "PRODUKSI"),
                ActivityPayload("ACT-SAP-102", "Loading Overburden", "m3", "Pemuatan tanah penutup ke dump truck", "PRODUKSI"),
                ActivityPayload("ACT-SAP-103", "Ripping Batuan Keras", "m3", "Pemecahan lapisan batuan menggunakan ripper dozer", "PRODUKSI"),
                ActivityPayload("ACT-SAP-201", "Dumping & Spreading", "m2", "Penghamparan material di area disposal", "SUPPORT"),
                ActivityPayload("ACT-SAP-202", "Grading Mainroad Tambang", "m", "Perawatan kerataan dan grading jalan hauling", "INFRASTRUKTUR"),
                ActivityPayload("ACT-SAP-203", "Clearing Lahan & Grubbing", "m2", "Pembersihan semak belukar dan vegetasi", "INFRASTRUKTUR"),
                ActivityPayload("ACT-SAP-301", "Hauling Material Subgrade", "ton", "Pengangkutan material perbaikan jalan", "HAULING"),
                ActivityPayload("ACT-SAP-302", "Pembuatan Parit Drainage", "m", "Penggalian parit saluran air tambang", "INFRASTRUKTUR")
            )
        )
    }

    // Central master operators in company web admin
    val serverOperators = CopyOnWriteArrayList<OperatorPayload>().apply {
        addAll(
            listOf(
                OperatorPayload("OPR-001", "Ahmad Fauzi", "NIK-94821", "Budi Santoso", "Senior Operator - Class A"),
                OperatorPayload("OPR-002", "Bambang Sudrajat", "NIK-88234", "Budi Santoso", "Senior Operator - Class A"),
                OperatorPayload("OPR-003", "Dedi Supriyadi", "NIK-77412", "Budi Santoso", "Operator Alat Berat - Class B"),
                OperatorPayload("OPR-004", "Eko Prasetyo", "NIK-65129", "Budi Santoso", "Operator Dump Truck - Class B"),
                OperatorPayload("OPR-005", "Hendra Gunawan", "NIK-91203", "Budi Santoso", "Master Grader Operator"),
                // Operators for Agus Wijaya
                OperatorPayload("OPR-006", "Indra Kusuma", "NIK-83190", "Agus Wijaya", "Senior Excavator Operator"),
                OperatorPayload("OPR-007", "Joko Susilo", "NIK-74102", "Agus Wijaya", "Dump Truck Driver"),
                // Operators for Rudi Hartono
                OperatorPayload("OPR-008", "Kurniawan Putra", "NIK-69214", "Rudi Hartono", "Dozer Operator")
            )
        )
    }

    // Central master lokasi in company web admin
    val serverLokasi = CopyOnWriteArrayList<LokasiPayload>().apply {
        addAll(
            listOf(
                LokasiPayload("001A", "PG1", 10.5, 8.2, "Area Front Utama"),
                LokasiPayload("002B", "PG1", 14.0, 11.5, "Disposal North"),
                LokasiPayload("100A", "PG2", 20.0, 16.8, "Pit Cut Site B"),
                LokasiPayload("101B", "PG2", 18.5, 15.0, "Loading Area 2"),
                LokasiPayload("201C", "PG3", 25.0, 21.4, "Hauling Route South"),
                LokasiPayload("MAINROAD-01", "INFRA", 30.0, 28.0, "Jalan Tambang Utama"),
                LokasiPayload("DISPOSAL-W", "DISP", 15.0, 12.0, "Penimbunan Barat"),
                LokasiPayload("PIT-NORTH", "PIT", 35.0, 30.5, "Pit Penambangan Utara")
            )
        )
    }

    // Central received reports in Web Admin
    val receivedReportsOnServer = CopyOnWriteArrayList<PostActivityReportRequest>()
    val serverRencanaKerja = CopyOnWriteArrayList<PostRencanaKerjaRequest>()
    val serverHasilAktivitas = CopyOnWriteArrayList<PostHasilAktivitasRequest>()

    suspend fun fetchMasterData(supervisorName: String): ApiResponse<MasterDataPayload> {
        delay(600) // network latency
        if (isSimulatedOffline) {
            throw java.io.IOException("Koneksi gagal: Perangkat berada di luar jangkauan jaringan (Offline Lapangan)")
        }

        // Supervisor isolation
        val filteredUnits = serverUnits.filter { it.resolvedSupervisor.equals(supervisorName, ignoreCase = true) }
        val filteredOperators = serverOperators.filter { it.resolvedSupervisor.equals(supervisorName, ignoreCase = true) }
        val activities = serverActivities.toList()
        val lokasi = serverLokasi.toList()

        return ApiResponse(
            status = "SUCCESS",
            message = "Data master berhasil disinkronkan dari Web Admin untuk pengawas: $supervisorName",
            data = MasterDataPayload(
                units = filteredUnits,
                activities = activities,
                aktivitas = activities,
                operators = filteredOperators,
                lokasi = lokasi,
                locations = lokasi
            )
        )
    }

    suspend fun postRencanaKerja(request: PostRencanaKerjaRequest): ApiResponse<String> {
        delay(500)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Rencana kerja tersimpan di lokal (MENUNGGU_SPK)")
        }
        serverRencanaKerja.add(request)
        val planId = request.id ?: System.currentTimeMillis()

        // Background simulation: Admin website reviews plan & publishes official SPK
        CoroutineScope(Dispatchers.Default).launch {
            delay(5000) // 5 seconds review simulation
            val spkNum = "SPK-2026-X" + (100 + (planId % 900))
            publishedSpkMap[planId] = spkNum
            val idx = serverRencanaKerja.indexOfFirst { it.id == request.id || it.kode_unit == request.kode_unit }
            if (idx >= 0) {
                val old = serverRencanaKerja[idx]
                serverRencanaKerja[idx] = old.copy(nomor_spk = spkNum, status_spk = "TERBIT_SPK")
            }
            _spkEventFlow.emit(
                SpkPublishedEvent(
                    planId = planId,
                    kodeUnit = request.kode_unit ?: "",
                    nomorSpk = spkNum,
                    supervisorName = request.nama_pengawas ?: ""
                )
            )
        }

        return ApiResponse(
            status = "SUCCESS",
            message = "Rencana kerja Unit ${request.kode_unit ?: ""} berhasil dicatat di Web Admin. Status: MENUNGGU_SPK",
            data = "MENUNGGU_SPK"
        )
    }

    /**
     * Manually or immediately triggers admin SPK publishing for a plan
     */
    suspend fun adminPublishSpk(planId: Long, kodeUnit: String, supervisorName: String): String {
        val spkNum = "SPK-2026-X" + (100 + (planId % 900))
        publishedSpkMap[planId] = spkNum
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId || it.kode_unit == kodeUnit }
        if (idx >= 0) {
            val old = serverRencanaKerja[idx]
            serverRencanaKerja[idx] = old.copy(nomor_spk = spkNum, status_spk = "TERBIT_SPK")
        }
        _spkEventFlow.emit(
            SpkPublishedEvent(
                planId = planId,
                kodeUnit = kodeUnit,
                nomorSpk = spkNum,
                supervisorName = supervisorName
            )
        )
        return spkNum
    }

    fun getPublishedSpk(planId: Long): String? = publishedSpkMap[planId]

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): ApiResponse<String> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil kerja disimpan ke penyimpanan lokal")
        }
        serverHasilAktivitas.add(request)

        val targetUnitCode = request.kode_unit ?: ""
        val lastHmVal = request.hm_akhir ?: 0.0

        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val fuelInfo = if (request.is_isi_solar == true) " [Solar: ${request.jumlah_liter_solar} L]" else ""
        return ApiResponse(
            status = "SUCCESS",
            message = "Realisasi Unit $targetUnitCode berhasil dikirim.$fuelInfo HM Unit diperbarui ke $lastHmVal.",
            data = "ACK-${System.currentTimeMillis()}"
        )
    }

    suspend fun postActivityReport(report: PostActivityReportRequest): ApiResponse<String> {
        delay(700)
        if (isSimulatedOffline) {
            throw java.io.IOException("Jaringan tidak tersedia: Laporan disimpan ke penyimpanan lokal perangkat")
        }

        // Add to server log
        receivedReportsOnServer.add(report)

        val targetUnitCode = report.kode_unit ?: report.unit_code ?: ""
        val lastHmVal = report.hm_akhir ?: 0.0

        // Update server unit last HM
        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val hasPhoto = !report.foto_bukti.isNullOrBlank() || !report.photo_base64.isNullOrBlank()
        val photoInfo = if (hasPhoto) " [Foto Bukti Kegiatan Terverifikasi (${report.photo_size_kb ?: 145} KB, ${report.photo_dimensions ?: "1280x960 px"})]" else ""
        val statusInfo = " [Status: ${report.status_unit ?: "OPERASI"}]"
        val fuelInfo = if (report.is_isi_solar == true) " [Solar: ${report.jumlah_liter_solar} L]" else ""

        return ApiResponse(
            status = "SUCCESS",
            message = "Laporan berhasil dikirim dan diverifikasi oleh Server Web Admin.$statusInfo$fuelInfo. HM Unit diperbarui ke $lastHmVal.$photoInfo",
            data = "REPORT-ACK-${System.currentTimeMillis()}"
        )
    }

    // Methods for Web Admin simulator (Tambah / Edit / Hapus Master Data)
    fun addOrUpdateUnit(unit: UnitPayload) {
        val idx = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(unit.resolvedUnitCode, ignoreCase = true) }
        if (idx >= 0) {
            serverUnits[idx] = unit
        } else {
            serverUnits.add(unit)
        }
    }

    fun deleteUnit(unitCode: String) {
        serverUnits.removeAll { it.resolvedUnitCode.equals(unitCode, ignoreCase = true) }
    }

    fun addOrUpdateActivity(activity: ActivityPayload) {
        val idx = serverActivities.indexOfFirst { it.resolvedSapCode.equals(activity.resolvedSapCode, ignoreCase = true) }
        if (idx >= 0) {
            serverActivities[idx] = activity
        } else {
            serverActivities.add(activity)
        }
    }

    fun deleteActivity(sapCode: String) {
        serverActivities.removeAll { it.resolvedSapCode.equals(sapCode, ignoreCase = true) }
    }

    fun addOrUpdateOperator(operator: OperatorPayload) {
        val idx = serverOperators.indexOfFirst { it.resolvedOperatorId.equals(operator.resolvedOperatorId, ignoreCase = true) }
        if (idx >= 0) {
            serverOperators[idx] = operator
        } else {
            serverOperators.add(operator)
        }
    }

    fun deleteOperator(operatorId: String) {
        serverOperators.removeAll { it.resolvedOperatorId.equals(operatorId, ignoreCase = true) }
    }
}
