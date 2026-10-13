package com.example.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class SpkPublishedEvent(
    val planId: Long,
    val kodeUnit: String,
    val nomorSpk: String,
    val supervisorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class RencanaDeletedEvent(
    val planId: Long,
    val unitKode: String,
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

    private val _rencanaDeletedEventFlow = MutableSharedFlow<RencanaDeletedEvent>(extraBufferCapacity = 64)
    val rencanaDeletedEventFlow = _rencanaDeletedEventFlow.asSharedFlow()

    // Map of planId -> published SPK
    val publishedSpkMap = ConcurrentHashMap<Long, String>()

    // Central master units in company web admin
    val serverUnits = CopyOnWriteArrayList<UnitPayload>().apply {
        addAll(
            listOf(
                UnitPayload("HEH1", "EXCAVATOR", "Komatsu PC200-8", 4125.5, "Budi Santoso", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("EXC-02", "EXCAVATOR", "Caterpillar 320D", 6280.0, "Budi Santoso", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("DT-101", "DUMP TRUCK", "Hino FM 260 Ti", 3450.2, "Budi Santoso", "OPERASIONAL", 5.5, 9.5, "Geodesi PG1"),
                UnitPayload("DT-102", "DUMP TRUCK", "Scania P360", 2890.4, "Budi Santoso", "OPERASIONAL", 5.5, 9.5, "Geodesi PG1"),
                UnitPayload("DZ-01", "BULLDOZER", "Komatsu D85ESS-2", 5130.8, "Budi Santoso", "OPERASIONAL", 6.0, 10.0, "Geodesi PG1"),
                UnitPayload("GRD-01", "MOTOR GRADER", "Caterpillar 140K", 1870.0, "Budi Santoso", "OPERASIONAL", 5.0, 9.0, "Geodesi PG1"),
                // Units assigned to other supervisors
                UnitPayload("EXC-09", "EXCAVATOR", "Hitachi ZX210-5G", 3990.2, "Agus Wijaya", "OPERASIONAL", 6.0, 10.0, "Geodesi PG2"),
                UnitPayload("DT-205", "DUMP TRUCK", "Volvo FMX 440", 4150.0, "Agus Wijaya", "OPERASIONAL", 5.5, 9.5, "Geodesi PG2"),
                UnitPayload("DZ-04", "BULLDOZER", "CAT D6R", 2980.5, "Rudi Hermawan", "OPERASIONAL", 6.0, 10.0, "RC PG1")
            )
        )
    }

    // Central master kendala operasional in company web admin (15 Item Standar Lapangan)
    val serverKendala = CopyOnWriteArrayList<KendalaPayload>().apply {
        addAll(
            listOf(
                KendalaPayload(id = 1, kode_kendala = "KND-01", nama_kendala = "Antar - jemput", kategori = "OPERASIONAL", deskripsi = "Mobilisasi personel antar - jemput shift"),
                KendalaPayload(id = 2, kode_kendala = "KND-02", nama_kendala = "Cek unit, Implement Pemanasan mesin", kategori = "OPERASIONAL", deskripsi = "Pemeriksaan P2H harian dan pemanasan unit"),
                KendalaPayload(id = 3, kode_kendala = "KND-03", nama_kendala = "Transport", kategori = "OPERASIONAL", deskripsi = "Perjalanan/transportasi unit ke front kerja"),
                KendalaPayload(id = 4, kode_kendala = "KND-04", nama_kendala = "Kerja", kategori = "OPERASIONAL", deskripsi = "Aktivitas kerja operasional"),
                KendalaPayload(id = 5, kode_kendala = "KND-05", nama_kendala = "Istirahat", kategori = "OPERASIONAL", deskripsi = "Waktu istirahat resmi operator"),
                KendalaPayload(id = 6, kode_kendala = "KND-06", nama_kendala = "Perbaikan Unit", kategori = "MEKANIKAL", deskripsi = "Perbaikan kerusakan pada unit alat berat"),
                KendalaPayload(id = 7, kode_kendala = "KND-07", nama_kendala = "Perbaikan Implement", kategori = "MEKANIKAL", deskripsi = "Perbaikan attachment/implement unit"),
                KendalaPayload(id = 8, kode_kendala = "KND-08", nama_kendala = "Perawatan Unit", kategori = "MEKANIKAL", deskripsi = "Servis rutin dan perawatan berkala unit"),
                KendalaPayload(id = 9, kode_kendala = "KND-09", nama_kendala = "Perawatan Implement", kategori = "MEKANIKAL", deskripsi = "Servis dan greasing implement"),
                KendalaPayload(id = 10, kode_kendala = "KND-10", nama_kendala = "Ganti Implement", kategori = "OPERASIONAL", deskripsi = "Penggantian bucket/blade/ripper attachment"),
                KendalaPayload(id = 11, kode_kendala = "KND-11", nama_kendala = "Tunggu Solar", kategori = "LOGISTIK", deskripsi = "Menunggu pengisian bahan bakar solar / fuel truck"),
                KendalaPayload(id = 12, kode_kendala = "KND-12", nama_kendala = "Tunggu Mekanik", kategori = "MEKANIKAL", deskripsi = "Menunggu kedatangan mekanik servis"),
                KendalaPayload(id = 13, kode_kendala = "KND-13", nama_kendala = "Tunggu Trailler", kategori = "LOGISTIK", deskripsi = "Menunggu lowbed / trailler mobilisasi unit"),
                KendalaPayload(id = 14, kode_kendala = "KND-14", nama_kendala = "Tunggu Lokasi", kategori = "OPERASIONAL", deskripsi = "Menunggu kesiapan front kerja / lokasi kerja"),
                KendalaPayload(id = 15, kode_kendala = "KND-15", nama_kendala = "Tunggu Cuaca", kategori = "CUACA", deskripsi = "Hambatan cuaca buruk / hujan lebat / slippery")
            )
        )
    }

    // Central master activities in company web admin
    val serverActivities = CopyOnWriteArrayList<ActivityPayload>().apply {
        addAll(
            listOf(
                ActivityPayload("ACT-SAP-101", "Galian Tanah Lunak", "m3", "Penggalian area cut site A", "PRODUKSI", "OPERASI", "EXCAVATOR"),
                ActivityPayload("ACT-SAP-102", "Loading Overburden", "m3", "Pemuatan tanah penutup ke dump truck", "PRODUKSI", "OPERASI", "EXCAVATOR"),
                ActivityPayload("ACT-SAP-103", "Ripping Batuan Keras", "m3", "Pemecahan lapisan batuan menggunakan ripper dozer", "PRODUKSI", "OPERASI", "BULLDOZER"),
                ActivityPayload("ACT-SAP-201", "Dumping & Spreading", "m2", "Penghamparan material di area disposal", "SUPPORT", "OPERASI", "DUMP TRUCK"),
                ActivityPayload("ACT-SAP-202", "Grading Mainroad Tambang", "m", "Perawatan kerataan dan grading jalan hauling", "INFRASTRUKTUR", "OPERASI", "MOTOR GRADER"),
                ActivityPayload("ACT-SAP-STB-01", "Standby Hujan / Cuaca Buruk", "jam", "Kondisi hujan lebat atau jalur licin", "STANDBY", "STANDBY", "ALL"),
                ActivityPayload("ACT-SAP-STB-02", "Standby Tunggu Trailler / Front", "jam", "Menunggu lowbed mobilisasi atau kesiapan front", "STANDBY", "STANDBY", "ALL"),
                ActivityPayload("ACT-SAP-BDN-01", "Breakdown Kerusakan Mesin / Engine", "jam", "Trouble mesin / transmisi unit", "BREAKDOWN", "BREAKDOWN", "ALL"),
                ActivityPayload("ACT-SAP-BDN-02", "Breakdown Perbaikan Undercarriage", "jam", "Perbaikan track / undercarriage unit", "BREAKDOWN", "BREAKDOWN", "ALL")
            )
        )
    }

    // Central master operators in company web admin
    val serverOperators = CopyOnWriteArrayList<OperatorPayload>().apply {
        addAll(
            listOf(
                OperatorPayload("OPR-001", "Ahmad Fauzi", "NIK-94821", "Budi Santoso", "Senior Operator - Class A", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-002", "Bambang Sudrajat", "NIK-88234", "Budi Santoso", "Senior Operator - Class A", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-003", "Dedi Supriyadi", "NIK-77412", "Budi Santoso", "Operator Alat Berat - Class B", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-004", "Eko Prasetyo", "NIK-65129", "Budi Santoso", "Operator Dump Truck - Class B", "AKTIF", "Geodesi PG1"),
                OperatorPayload("OPR-005", "Hendra Gunawan", "NIK-91203", "Budi Santoso", "Master Grader Operator", "AKTIF", "Geodesi PG1"),
                // Operators for Agus Wijaya
                OperatorPayload("OPR-006", "Indra Kusuma", "NIK-83190", "Agus Wijaya", "Senior Excavator Operator", "AKTIF", "Geodesi PG2"),
                OperatorPayload("OPR-007", "Joko Susilo", "NIK-74102", "Agus Wijaya", "Dump Truck Driver", "AKTIF", "Geodesi PG2"),
                // Operators for Rudi Hermawan
                OperatorPayload("OPR-008", "Kurniawan Putra", "NIK-69214", "Rudi Hermawan", "Dozer Operator", "AKTIF", "RC PG1")
            )
        )
    }

    // Central master lokasi in company web admin
    val serverLokasi = CopyOnWriteArrayList<LokasiPayload>().apply {
        addAll(
            listOf(
                LokasiPayload("001A", "Geodesi PG1", 10.5, 8.2, "Area Front Utama"),
                LokasiPayload("002B", "Geodesi PG1", 14.0, 11.5, "Disposal North"),
                LokasiPayload("100A", "Geodesi PG2", 20.0, 16.8, "Pit Cut Site B"),
                LokasiPayload("101B", "Geodesi PG2", 18.5, 15.0, "Loading Area 2"),
                LokasiPayload("201C", "RC PG1", 25.0, 21.4, "Hauling Route South"),
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
        val kendala = serverKendala.toList()

        return ApiResponse(
            status = "SUCCESS",
            message = "Data master berhasil disinkronkan dari Web Admin untuk pengawas: $supervisorName",
            data = MasterDataPayload(
                units = filteredUnits,
                activities = activities,
                aktivitas = activities,
                operators = filteredOperators,
                lokasi = lokasi,
                locations = lokasi,
                kendala = kendala
            )
        )
    }

    suspend fun fetchBootstrapData(pengawas: String, tanggal: String): ApiResponse<BootstrapDataPayload> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Koneksi gagal: Perangkat berada di luar jangkauan jaringan (Offline Lapangan)")
        }

        // 1. Filter Master Data for supervisor
        val filteredUnits = serverUnits.filter { it.resolvedSupervisor.equals(pengawas, ignoreCase = true) }
        val filteredOperators = serverOperators.filter { it.resolvedSupervisor.equals(pengawas, ignoreCase = true) }
        val activities = serverActivities.toList()
        val lokasi = serverLokasi.toList()
        val kendala = serverKendala.toList()
        val masterData = MasterDataPayload(
            units = filteredUnits,
            activities = activities,
            aktivitas = activities,
            operators = filteredOperators,
            lokasi = lokasi,
            locations = lokasi,
            kendala = kendala
        )

        // 2. Ensure initial work plans on server for this supervisor if none yet
        if (serverRencanaKerja.none { it.nama_pengawas.equals(pengawas, ignoreCase = true) }) {
            // Seed realistic work plans issued by Admin with SPK Terbit
            serverRencanaKerja.addAll(
                listOf(
                    PostRencanaKerjaRequest(
                        id = "101",
                        status_unit = "OPERASI",
                        kode_unit = "HEH1",
                        operator = "Ahmad Fauzi",
                        sap_code = "ACT-SAP-101",
                        nama_aktivitas = "Galian Tanah Lunak",
                        satuan_aktivitas = "m3",
                        kode_lokasi = "001A",
                        shift_kerja = "SIANG",
                        keterangan = "Prioritas penggalian front area utara cut site",
                        nomor_spk = "SPK-2026-X101",
                        status_spk = "TERBIT_SPK",
                        status_tahap = "RENCANA",
                        is_realized = false,
                        nama_pengawas = pengawas
                    ),
                    PostRencanaKerjaRequest(
                        id = "102",
                        status_unit = "OPERASI",
                        kode_unit = "EXC-02",
                        operator = "Bambang Sudrajat",
                        sap_code = "ACT-SAP-102",
                        nama_aktivitas = "Loading Overburden",
                        satuan_aktivitas = "m3",
                        kode_lokasi = "002B",
                        shift_kerja = "SIANG",
                        keterangan = "Pemuatan overburden ke fleet dump truck",
                        nomor_spk = "SPK-2026-X102",
                        status_spk = "TERBIT_SPK",
                        status_tahap = "RENCANA",
                        is_realized = false,
                        nama_pengawas = pengawas
                    ),
                    PostRencanaKerjaRequest(
                        id = "103",
                        status_unit = "OPERASI",
                        kode_unit = "DT-101",
                        operator = "Dedi Supriyadi",
                        sap_code = "ACT-SAP-301",
                        nama_aktivitas = "Hauling Material Subgrade",
                        satuan_aktivitas = "ton",
                        kode_lokasi = "MAINROAD-01",
                        shift_kerja = "SIANG",
                        keterangan = "Hauling subgrade perbaikan jalan hauling",
                        nomor_spk = "SPK-2026-X103",
                        status_spk = "TERBIT_SPK",
                        status_tahap = "RENCANA",
                        is_realized = false,
                        nama_pengawas = pengawas
                    )
                )
            )
        }

        // Active plans for supervisor
        val activePlans = serverRencanaKerja.filter {
            it.nama_pengawas.equals(pengawas, ignoreCase = true) &&
            it.is_realized != true &&
            it.status_tahap == "RENCANA"
        }

        // 3. Ensure initial historical activity reports on server for this supervisor if none yet
        if (receivedReportsOnServer.none { it.nama_pengawas.equals(pengawas, ignoreCase = true) || it.supervisor_name.equals(pengawas, ignoreCase = true) }) {
            receivedReportsOnServer.addAll(
                listOf(
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
                        kode_unit = "HEH1",
                        jenis_unit = "EXCAVATOR",
                        nama_aktivitas = "Galian Tanah Lunak",
                        kode_sap = "ACT-SAP-101",
                        satuan = "m3",
                        nama_operator = "Ahmad Fauzi",
                        lokasi_kerja = "001A",
                        shift = "SIANG",
                        durasi_jam_kerja = 7.0,
                        hm_awal = 4118.0,
                        hm_akhir = 4125.5,
                        hm_harian_berjalan = 7.5,
                        hasil_kerja = 450.0,
                        keterangan = "Penggalian front cut berjalan lancar dan aman.",
                        status_unit = "OPERASI",
                        is_isi_solar = true,
                        jumlah_liter_solar = 120.0,
                        stik_solar_awal = 80.0,
                        stik_solar_akhir = 48.0,
                        pemakaian_solar = 152.0,
                        client_uuid = "SRV-REP-$pengawas-HEH1-4125.5",
                        client_transaction_id = "TX-SRV-001"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
                        kode_unit = "DZ-01",
                        jenis_unit = "BULLDOZER",
                        nama_aktivitas = "Dumping & Spreading",
                        kode_sap = "ACT-SAP-201",
                        satuan = "m2",
                        nama_operator = "Bambang Sudrajat",
                        lokasi_kerja = "002B",
                        shift = "SIANG",
                        durasi_jam_kerja = 7.0,
                        hm_awal = 5123.0,
                        hm_akhir = 5130.8,
                        hm_harian_berjalan = 7.8,
                        hasil_kerja = 620.0,
                        keterangan = "Perataan disposal area utara selesai sesuai target.",
                        status_unit = "OPERASI",
                        is_isi_solar = true,
                        jumlah_liter_solar = 140.0,
                        stik_solar_awal = 75.0,
                        stik_solar_akhir = 42.0,
                        pemakaian_solar = 173.0,
                        client_uuid = "SRV-REP-$pengawas-DZ01-5130.8",
                        client_transaction_id = "TX-SRV-002"
                    ),
                    PostActivityReportRequest(
                        tanggal = tanggal,
                        nama_pengawas = pengawas,
                        kode_unit = "GRD-01",
                        jenis_unit = "MOTOR GRADER",
                        nama_aktivitas = "Grading Mainroad Tambang",
                        kode_sap = "ACT-SAP-202",
                        satuan = "m",
                        nama_operator = "Hendra Gunawan",
                        lokasi_kerja = "MAINROAD-01",
                        shift = "SIANG",
                        durasi_jam_kerja = 6.5,
                        hm_awal = 1863.5,
                        hm_akhir = 1870.0,
                        hm_harian_berjalan = 6.5,
                        hasil_kerja = 1250.0,
                        keterangan = "Perawatan dan grading mainroad hauling jalan lancar.",
                        status_unit = "OPERASI",
                        is_isi_solar = false,
                        jumlah_liter_solar = 0.0,
                        client_uuid = "SRV-REP-$pengawas-GRD01-1870.0",
                        client_transaction_id = "TX-SRV-003"
                    )
                )
            )
        }

        val supervisorReports = receivedReportsOnServer.filter {
            it.nama_pengawas.equals(pengawas, ignoreCase = true) ||
            it.supervisor_name.equals(pengawas, ignoreCase = true)
        }

        return ApiResponse(
            status = "SUCCESS",
            message = "Data bootstrap operasional pengawas $pengawas berhasil diambil (${activePlans.size} rencana, ${supervisorReports.size} riwayat)",
            data = BootstrapDataPayload(
                master_data = masterData,
                masterData = masterData,
                rencana_aktif = activePlans,
                rencanaAktif = activePlans,
                riwayat_aktivitas = supervisorReports,
                riwayatAktivitas = supervisorReports
            )
        )
    }

    suspend fun getActiveRencanaKerja(pengawas: String): ApiResponse<List<PostRencanaKerjaRequest>> {
        delay(200)
        if (serverRencanaKerja.none { it.nama_pengawas.equals(pengawas, ignoreCase = true) }) {
            fetchBootstrapData(pengawas, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
        }
        val activePlans = serverRencanaKerja.filter {
            (it.nama_pengawas.isNullOrBlank() || it.nama_pengawas.equals(pengawas, ignoreCase = true)) &&
            it.is_realized != true
        }
        return ApiResponse(
            status = "SUCCESS",
            message = "Rencana kerja aktif pengawas $pengawas berhasil diambil (${activePlans.size} rencana)",
            data = activePlans
        )
    }

    suspend fun postRencanaKerja(request: PostRencanaKerjaRequest): ApiResponse<String> {
        delay(500)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Rencana kerja tersimpan di lokal (MENUNGGU_SPK)")
        }
        serverRencanaKerja.add(request)

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
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId.toString() || it.numericId == planId || it.kode_unit == kodeUnit }
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

    /**
     * Manually triggers admin deleting a work plan (simulates Web Admin deletion)
     */
    suspend fun adminDeleteRencana(planId: Long, unitKode: String, supervisorName: String = "") {
        serverRencanaKerja.removeIf {
            (planId > 0 && (it.numericId == planId || it.id == planId.toString())) ||
            (unitKode.isNotBlank() && it.resolvedUnitKode.equals(unitKode, ignoreCase = true))
        }
        publishedSpkMap.remove(planId)
        _rencanaDeletedEventFlow.emit(
            RencanaDeletedEvent(
                planId = planId,
                unitKode = unitKode,
                supervisorName = supervisorName
            )
        )
    }

    fun markRencanaRealized(planId: Long) {
        val idx = serverRencanaKerja.indexOfFirst { it.id == planId.toString() || it.numericId == planId }
        if (idx >= 0) {
            val old = serverRencanaKerja[idx]
            serverRencanaKerja[idx] = old.copy(
                is_realized = true,
                status_tahap = "REALISASI_SELESAI",
                status_spk = "SELESAI"
            )
        }
    }

    fun getPublishedSpk(planId: Long): String? = publishedSpkMap[planId]

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): ApiResponse<String> {
        delay(600)
        if (isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil kerja disimpan ke penyimpanan lokal")
        }

        // Anti-duplikasi di server simulator
        val txId = request.client_transaction_id
        if (!txId.isNullOrBlank() && serverHasilAktivitas.any { it.client_transaction_id == txId }) {
            return ApiResponse(
                status = "SUCCESS",
                message = "Realisasi Unit ${request.kode_unit ?: ""} sudah tercatat di Web Admin (Anti-Duplikat Terverifikasi).",
                data = "ACK-IDEMPOTENT"
            )
        }

        serverHasilAktivitas.add(request)

        val targetUnitCode = request.kode_unit ?: ""
        val lastHmVal = request.hm_akhir ?: 0.0

        val index = serverUnits.indexOfFirst { it.resolvedUnitCode.equals(targetUnitCode, ignoreCase = true) }
        if (index >= 0) {
            val old = serverUnits[index]
            serverUnits[index] = old.copy(last_hm = lastHmVal, lastHm = lastHmVal)
        }

        val kendalaItems = request.kendala_lapangan ?: request.kendala_list ?: request.daftar_kendala
        val kendalaInfo = if (!kendalaItems.isNullOrEmpty()) {
            " [Kendala: ${kendalaItems.joinToString { "${it.nama_kendala} [${it.kode_kendala}]" }}]"
        } else ""
        val fuelInfo = if (request.is_isi_solar == true) " [Solar: ${request.jumlah_liter_solar} L]" else ""
        val auditInfo = if (request.is_hm_awal_corrected == true) " [AUDIT ADMIN: Koreksi HM Awal Manual (Alasan: ${request.alasan_koreksi_hm ?: "-"})]" else ""
        return ApiResponse(
            status = "SUCCESS",
            message = "Realisasi Unit $targetUnitCode berhasil dikirim.$kendalaInfo$fuelInfo$auditInfo HM Unit diperbarui ke $lastHmVal.",
            data = "ACK-${System.currentTimeMillis()}"
        )
    }

    suspend fun postActivityReport(report: PostActivityReportRequest): ApiResponse<String> {
        delay(700)
        if (isSimulatedOffline) {
            throw java.io.IOException("Jaringan tidak tersedia: Laporan disimpan ke penyimpanan lokal perangkat")
        }

        // Anti-duplikasi di server simulator
        val clientUuidVal = report.client_uuid ?: report.client_transaction_id
        if (!clientUuidVal.isNullOrBlank() && receivedReportsOnServer.any { (it.client_uuid ?: it.client_transaction_id) == clientUuidVal }) {
            return ApiResponse(
                status = "SUCCESS",
                message = "Laporan sudah tercatat sebelumnya di server Web Admin (Anti-Duplikat Terverifikasi).",
                data = "REPORT-ACK-IDEMPOTENT"
            )
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
        val rptKendalaItems = report.kendala_lapangan ?: report.kendala_list ?: report.daftar_kendala
        val rptKendalaInfo = if (!rptKendalaItems.isNullOrEmpty()) {
            " [Kendala: ${rptKendalaItems.joinToString { "${it.nama_kendala} [${it.kode_kendala}]" }}]"
        } else ""
        val statusInfo = " [Status: ${report.status_unit ?: "OPERASI"}]"
        val fuelInfo = if (report.is_isi_solar == true) " [Solar: ${report.jumlah_liter_solar} L]" else ""
        val auditInfo = if (report.is_hm_awal_corrected == true) " [AUDIT: Koreksi HM Awal (${report.hm_awal}) Alasan: ${report.alasan_koreksi_hm ?: "-"}]" else ""

        return ApiResponse(
            status = "SUCCESS",
            message = "Laporan berhasil dikirim dan diverifikasi oleh Server Web Admin.$rptKendalaInfo$statusInfo$fuelInfo$auditInfo. HM Unit diperbarui ke $lastHmVal.$photoInfo",
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

    suspend fun authenticate(username: String, pin: String): LoginResult {
        delay(400) // latency simulation
        val trimmedUser = username.trim()
        val trimmedPin = pin.trim()

        if (trimmedUser.contains("nonaktif", ignoreCase = true) || trimmedUser.contains("inactive", ignoreCase = true)) {
            return LoginResult.Error("Akun tidak aktif atau kredensial salah")
        }

        val matched = com.example.data.model.SupervisorDirectory.authenticate(trimmedUser, trimmedPin)
        if (matched != null) {
            if (!matched.isAktif) {
                return LoginResult.Error("Akun tidak aktif atau kredensial salah")
            }
            return LoginResult.Success(
                token = "SIM-JWT-${System.currentTimeMillis()}",
                user = UserProfileDto(
                    id = matched.userId,
                    nama_lengkap = matched.namaLengkap,
                    username = matched.username,
                    role = matched.role,
                    wilayah = matched.wilayah,
                    status = matched.status
                ),
                message = "Login berhasil sebagai ${matched.role}"
            )
        }

        // Standalone user fallback jika sandi diisi (e.g. 1234 atau >= 4 karakter)
        if (trimmedPin == "1234" || trimmedPin.length >= 4) {
            val isSuperAdmin = trimmedUser.contains("super", ignoreCase = true) || trimmedUser.contains("admin", ignoreCase = true)
            val isMgr = isSuperAdmin || trimmedUser.contains("manager", ignoreCase = true) || trimmedUser.startsWith("mgr", ignoreCase = true) || trimmedUser.startsWith("gm", ignoreCase = true)
            val assignedRole = when {
                isSuperAdmin -> "SUPER ADMIN"
                isMgr -> "MANAGER"
                else -> "KASIE"
            }
            val assignedWilayah = when {
                isMgr || isSuperAdmin -> "Semua Wilayah"
                else -> "Geodesi 1"
            }

            return LoginResult.Success(
                token = "SIM-JWT-${System.currentTimeMillis()}",
                user = UserProfileDto(
                    id = System.currentTimeMillis().toString().takeLast(4),
                    nama_lengkap = trimmedUser,
                    username = trimmedUser,
                    role = assignedRole,
                    wilayah = assignedWilayah,
                    status = "Aktif"
                ),
                message = "Login berhasil sebagai $assignedRole"
            )
        }

        return LoginResult.Error("Akun tidak aktif atau kredensial salah")
    }
}
