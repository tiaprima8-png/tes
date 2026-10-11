package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.ActivityReportEntity
import com.example.data.local.entity.HasilInputAktivitasEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.remote.ApiClientManager
import com.example.data.remote.CompanyServerSimulator
import com.example.data.remote.KendalaJsonConverter
import com.example.data.remote.PostActivityReportRequest
import com.example.data.remote.PostHasilAktivitasRequest
import com.example.data.remote.PostRencanaKerjaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SyncLogItem(
    val timestamp: String,
    val endpoint: String,
    val method: String,
    val status: String,
    val detail: String,
    val isSuccess: Boolean
)

class HeavyTrackRepository(
    private val database: AppDatabase,
    val apiClientManager: ApiClientManager
) {
    private val unitDao = database.unitDao()
    private val activityDao = database.activityMasterDao()
    private val operatorDao = database.operatorDao()
    private val reportDao = database.activityReportDao()
    private val lokasiDao = database.lokasiDao()
    private val rencanaDao = database.rencanaKerjaDao()
    private val hasilDao = database.hasilInputAktivitasDao()
    private val kendalaDao = database.kendalaDao()
    private val draftDao = database.draftRealisasiDao()

    private val _syncLogs = MutableStateFlow<List<SyncLogItem>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogItem>> = _syncLogs.asStateFlow()

    private val _lastSyncTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimeMillis: StateFlow<Long> = _lastSyncTimeMillis.asStateFlow()

    private fun addLog(endpoint: String, method: String, status: String, detail: String, isSuccess: Boolean) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val item = SyncLogItem(time, endpoint, method, status, detail, isSuccess)
        _syncLogs.value = listOf(item) + _syncLogs.value.take(20)
    }

    fun getUnitsForSupervisor(supervisor: String): Flow<List<UnitEntity>> =
        unitDao.getUnitsForSupervisor(supervisor)

    fun getAllUnits(): Flow<List<UnitEntity>> =
        unitDao.getAllUnits()

    fun getAllActivities(): Flow<List<ActivityMasterEntity>> =
        activityDao.getAllActivities()

    fun getOperatorsForSupervisor(supervisor: String): Flow<List<OperatorEntity>> =
        operatorDao.getOperatorsForSupervisor(supervisor)

    fun getAllLokasi(): Flow<List<LokasiEntity>> =
        lokasiDao.getAllLokasi()

    fun searchLokasi(query: String): Flow<List<LokasiEntity>> =
        lokasiDao.searchLokasi(query)

    fun getRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getRencanaForSupervisor(supervisor)

    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>> =
        rencanaDao.getActiveRencanaForSupervisor(supervisor)

    fun getHasilForSupervisor(supervisor: String): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getHasilForSupervisor(supervisor)

    fun getAllHasil(): Flow<List<HasilInputAktivitasEntity>> =
        hasilDao.getAllHasil()

    fun getAllKendala(): Flow<List<KendalaEntity>> =
        kendalaDao.getAllKendala()

    fun getAllReports(): Flow<List<ActivityReportEntity>> =
        reportDao.getAllReports()

    fun getReportsForSupervisor(supervisor: String): Flow<List<ActivityReportEntity>> =
        reportDao.getReportsForSupervisor(supervisor)

    fun getUnsyncedReports(): Flow<List<ActivityReportEntity>> =
        reportDao.getUnsyncedReports()

    fun getUnsyncedCount(): Flow<Int> =
        combine(reportDao.getUnsyncedCount(), hasilDao.getUnsyncedCount()) { repCount, hasCount ->
            maxOf(repCount, hasCount)
        }

    suspend fun getUnsyncedCountValue(): Int = withContext(Dispatchers.IO) {
        val repCount = reportDao.getUnsyncedReportsList().count { !it.isSynced }
        val hasCount = hasilDao.getUnsyncedHasilList().count { !it.isSynced || it.statusKirim == "PENDING" }
        maxOf(repCount, hasCount)
    }

    suspend fun reconcileSyncStatus() = withContext(Dispatchers.IO) {
        try {
            val allHasil = hasilDao.getAllHasil().firstOrNull() ?: emptyList()
            val unsyncedReports = reportDao.getUnsyncedReportsList()
            for (report in unsyncedReports) {
                val matchingSyncedHasil = allHasil.find {
                    it.isSynced && it.kodeUnit == report.unitCode && it.hmAkhir == report.hmAkhir
                }
                if (matchingSyncedHasil != null) {
                    val syncTime = matchingSyncedHasil.syncedAt ?: System.currentTimeMillis()
                    reportDao.markReportAsSynced(report.id, syncTime)
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun getUnitByCode(unitCode: String): UnitEntity? =
        unitDao.getUnitByCode(unitCode)

    suspend fun getLatestRealizedHmForUnit(unitCode: String): Double? = withContext(Dispatchers.IO) {
        val latestHasil = hasilDao.getLatestHasilForUnit(unitCode)
        if (latestHasil != null && latestHasil.hmAkhir > 0.0) {
            return@withContext latestHasil.hmAkhir
        }
        val unit = unitDao.getUnitByCode(unitCode)
        return@withContext unit?.lastHm
    }

    suspend fun seedInitialDataIfEmpty(activeSupervisor: String) = withContext(Dispatchers.IO) {
        try {
            fetchMasterData(activeSupervisor)
        } catch (_: Exception) {
            val initialUnits = listOf(
                UnitEntity("HEH1", "EXCAVATOR", "Komatsu PC200-8", 4125.5, activeSupervisor, "OPERASIONAL", 6.0, 10.0),
                UnitEntity("EXC-02", "EXCAVATOR", "Caterpillar 320D", 6280.0, activeSupervisor, "OPERASIONAL", 6.0, 10.0),
                UnitEntity("DT-101", "DUMP TRUCK", "Hino FM 260 Ti", 3450.2, activeSupervisor, "OPERASIONAL", 5.5, 9.5),
                UnitEntity("DZ-01", "BULLDOZER", "Komatsu D85ESS-2", 5130.8, activeSupervisor, "OPERASIONAL", 6.0, 10.0)
            )
            unitDao.insertUnits(initialUnits)

            val initialActivities = listOf(
                ActivityMasterEntity("ACT-SAP-101", "Galian Tanah Lunak", "m3", "Galian area cut", "PRODUKSI"),
                ActivityMasterEntity("ACT-SAP-102", "Loading Overburden", "m3", "Pemuatan tanah penutup", "PRODUKSI"),
                ActivityMasterEntity("ACT-SAP-201", "Dumping & Spreading", "m2", "Penghamparan disposal", "SUPPORT"),
                ActivityMasterEntity("ACT-SAP-202", "Grading Mainroad Tambang", "m", "Perawatan jalan", "INFRASTRUKTUR")
            )
            activityDao.insertActivities(initialActivities)

            val initialOperators = listOf(
                OperatorEntity("OPR-001", "Ahmad Fauzi", "NIK-94821", activeSupervisor),
                OperatorEntity("OPR-002", "Bambang Sudrajat", "NIK-88234", activeSupervisor),
                OperatorEntity("OPR-003", "Dedi Supriyadi", "NIK-77412", activeSupervisor)
            )
            operatorDao.insertOperators(initialOperators)

            val initialLokasi = listOf(
                LokasiEntity("001A", "PG1", 10.5, 8.2, "Area Front Utama"),
                LokasiEntity("002B", "PG1", 14.0, 11.5, "Disposal North"),
                LokasiEntity("100A", "PG2", 20.0, 16.8, "Pit Cut Site B"),
                LokasiEntity("101B", "PG2", 18.5, 15.0, "Loading Area 2"),
                LokasiEntity("201C", "PG3", 25.0, 21.4, "Hauling Route South"),
                LokasiEntity("MAINROAD-01", "INFRA", 30.0, 28.0, "Jalan Tambang Utama")
            )
            lokasiDao.insertLokasiList(initialLokasi)

            val initialKendala = listOf(
                KendalaEntity(kodeKendala = "KND-01", namaKendala = "Antar - jemput", kategori = "OPERASIONAL", deskripsi = "Mobilisasi personel antar - jemput"),
                KendalaEntity(kodeKendala = "KND-02", namaKendala = "Cek unit, Implement Pemanasan mesin", kategori = "OPERASIONAL", deskripsi = "P2H dan pemanasan unit"),
                KendalaEntity(kodeKendala = "KND-03", namaKendala = "Transport", kategori = "OPERASIONAL", deskripsi = "Transport unit ke front kerja"),
                KendalaEntity(kodeKendala = "KND-04", namaKendala = "Kerja", kategori = "OPERASIONAL", deskripsi = "Aktivitas kerja"),
                KendalaEntity(kodeKendala = "KND-05", namaKendala = "Istirahat", kategori = "OPERASIONAL", deskripsi = "Istirahat operator"),
                KendalaEntity(kodeKendala = "KND-06", namaKendala = "Perbaikan Unit", kategori = "MEKANIKAL", deskripsi = "Perbaikan kerusakan unit"),
                KendalaEntity(kodeKendala = "KND-07", namaKendala = "Perbaikan Implement", kategori = "MEKANIKAL", deskripsi = "Perbaikan attachment/implement"),
                KendalaEntity(kodeKendala = "KND-08", namaKendala = "Perawatan Unit", kategori = "MEKANIKAL", deskripsi = "Servis berkala unit"),
                KendalaEntity(kodeKendala = "KND-09", namaKendala = "Perawatan Implement", kategori = "MEKANIKAL", deskripsi = "Perawatan attachment/implement"),
                KendalaEntity(kodeKendala = "KND-10", namaKendala = "Ganti Implement", kategori = "OPERASIONAL", deskripsi = "Ganti attachment unit"),
                KendalaEntity(kodeKendala = "KND-11", namaKendala = "Tunggu Solar", kategori = "LOGISTIK", deskripsi = "Menunggu pengisian bahan bakar"),
                KendalaEntity(kodeKendala = "KND-12", namaKendala = "Tunggu Mekanik", kategori = "MEKANIKAL", deskripsi = "Menunggu kedatangan mekanik"),
                KendalaEntity(kodeKendala = "KND-13", namaKendala = "Tunggu Trailler", kategori = "LOGISTIK", deskripsi = "Menunggu lowbed / trailler"),
                KendalaEntity(kodeKendala = "KND-14", namaKendala = "Tunggu Lokasi", kategori = "OPERASIONAL", deskripsi = "Menunggu kesiapan front kerja"),
                KendalaEntity(kodeKendala = "KND-15", namaKendala = "Tunggu Cuaca", kategori = "CUACA", deskripsi = "Menunggu cuaca membaik / hujan / slippery")
            )
            kendalaDao.clearAll()
            kendalaDao.insertKendalaList(initialKendala)
        }
    }

    suspend fun fetchMasterData(supervisorName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (payload, message) = apiClientManager.fetchMasterData(supervisorName)

            // 1. Process Units
            val incomingUnits = payload.units ?: emptyList()
            if (incomingUnits.isNotEmpty()) {
                val unitEntities = incomingUnits.map {
                    UnitEntity(
                        unitCode = it.resolvedUnitCode,
                        unitType = it.resolvedUnitType,
                        brandModel = it.resolvedBrandModel,
                        lastHm = it.resolvedLastHm,
                        supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                        status = it.resolvedStatus,
                        hmMinStandar = it.resolvedHmMinStandar,
                        hmMaxStandar = it.resolvedHmMaxStandar,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                unitDao.insertUnits(unitEntities)
            }

            // 2. Process Activities
            val incomingActivities = payload.resolvedActivities
            if (incomingActivities.isNotEmpty()) {
                val actEntities = incomingActivities.map {
                    ActivityMasterEntity(
                        sapCode = it.resolvedSapCode,
                        activityName = it.resolvedActivityName,
                        unitOfMeasure = it.resolvedUnitOfMeasure,
                        description = it.resolvedDescription,
                        category = it.resolvedCategory,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                activityDao.clearAllActivities()
                activityDao.insertActivities(actEntities)
            }

            // 3. Process Operators
            val incomingOperators = payload.operators ?: emptyList()
            if (incomingOperators.isNotEmpty()) {
                val opEntities = incomingOperators.map {
                    OperatorEntity(
                        operatorId = it.resolvedOperatorId,
                        name = it.resolvedName,
                        badgeNumber = it.resolvedBadgeNumber,
                        supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                        skillLevel = it.resolvedSkillLevel,
                        status = it.resolvedStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                operatorDao.insertOperators(opEntities)
            }

            // 4. Process Lokasi
            val incomingLokasi = payload.resolvedLokasi
            if (incomingLokasi.isNotEmpty()) {
                val lokasiEntities = incomingLokasi.map {
                    LokasiEntity(
                        kodeLokasi = it.resolvedKode,
                        wilayah = it.resolvedWilayah,
                        luasBruto = it.resolvedLuasBruto,
                        luasNetto = it.resolvedLuasNetto,
                        keterangan = it.keterangan ?: "",
                        updatedAt = System.currentTimeMillis()
                    )
                }
                lokasiDao.insertLokasiList(lokasiEntities)
            }

            // 5. Process Kendala
            val incomingKendala = payload.kendala ?: emptyList()
            if (incomingKendala.isNotEmpty()) {
                val kendalaEntities = incomingKendala.mapIndexed { index, it ->
                    KendalaEntity(
                        kodeKendala = it.resolveKode(index),
                        namaKendala = it.resolvedNama,
                        kategori = it.resolvedKategori,
                        deskripsi = it.resolvedDeskripsi,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                kendalaDao.clearAll()
                kendalaDao.insertKendalaList(kendalaEntities)
            } else {
                // Jika payload kendala kosong dari API luar, pastikan database lokal memiliki 15 master kendala standar
                val currentCount = kendalaDao.getAllKendala()
                // Tidak menghapus jika lokal sudah ada data
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            val summary = "${incomingUnits.size} Unit, ${incomingActivities.size} Aktivitas, ${incomingOperators.size} Operator, ${incomingLokasi.size} Lokasi, ${incomingKendala.size} Kendala"
            addLog("GET /api/master-data", "GET", "200 OK", summary, true)

            Result.success("$message ($summary)")
        } catch (e: Exception) {
            addLog("GET /api/master-data", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun fetchBootstrapData(supervisorName: String, tanggal: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val (payload, message) = apiClientManager.fetchBootstrapData(supervisorName, tanggal)

            // 1. Process Master Data
            val master = payload.resolvedMasterData

            val incomingUnits = master.units ?: emptyList()
            if (incomingUnits.isNotEmpty()) {
                val unitEntities = incomingUnits.map {
                    UnitEntity(
                        unitCode = it.resolvedUnitCode,
                        unitType = it.resolvedUnitType,
                        brandModel = it.resolvedBrandModel,
                        lastHm = it.resolvedLastHm,
                        supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                        status = it.resolvedStatus,
                        hmMinStandar = it.resolvedHmMinStandar,
                        hmMaxStandar = it.resolvedHmMaxStandar,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                unitDao.insertUnits(unitEntities)
            }

            val incomingActivities = master.resolvedActivities
            if (incomingActivities.isNotEmpty()) {
                val actEntities = incomingActivities.map {
                    ActivityMasterEntity(
                        sapCode = it.resolvedSapCode,
                        activityName = it.resolvedActivityName,
                        unitOfMeasure = it.resolvedUnitOfMeasure,
                        description = it.resolvedDescription,
                        category = it.resolvedCategory,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                activityDao.clearAllActivities()
                activityDao.insertActivities(actEntities)
            }

            val incomingOperators = master.operators ?: emptyList()
            if (incomingOperators.isNotEmpty()) {
                val opEntities = incomingOperators.map {
                    OperatorEntity(
                        operatorId = it.resolvedOperatorId,
                        name = it.resolvedName,
                        badgeNumber = it.resolvedBadgeNumber,
                        supervisorAssigned = if (it.resolvedSupervisor.isNotBlank()) it.resolvedSupervisor else supervisorName,
                        skillLevel = it.resolvedSkillLevel,
                        status = it.resolvedStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                operatorDao.insertOperators(opEntities)
            }

            val incomingLokasi = master.resolvedLokasi
            if (incomingLokasi.isNotEmpty()) {
                val lokasiEntities = incomingLokasi.map {
                    LokasiEntity(
                        kodeLokasi = it.resolvedKode,
                        wilayah = it.resolvedWilayah,
                        luasBruto = it.resolvedLuasBruto,
                        luasNetto = it.resolvedLuasNetto,
                        keterangan = it.keterangan ?: "",
                        updatedAt = System.currentTimeMillis()
                    )
                }
                lokasiDao.insertLokasiList(lokasiEntities)
            }

            val incomingKendala = master.kendala ?: emptyList()
            if (incomingKendala.isNotEmpty()) {
                val kendalaEntities = incomingKendala.mapIndexed { index, it ->
                    KendalaEntity(
                        kodeKendala = it.resolveKode(index),
                        namaKendala = it.resolvedNama,
                        kategori = it.resolvedKategori,
                        deskripsi = it.resolvedDeskripsi,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                kendalaDao.clearAll()
                kendalaDao.insertKendalaList(kendalaEntities)
            }

            // 2. Process Rencana Aktif -> Insert / Upsert into rencana_kerja
            val incomingRencana = payload.resolvedRencanaAktif
            var rencanaUpsertedCount = 0
            for (renReq in incomingRencana) {
                val kodeUnitVal = renReq.kode_unit ?: continue
                val existing = rencanaDao.findRencana(supervisorName, kodeUnitVal, tanggal)
                val entity = RencanaKerjaEntity(
                    id = existing?.id ?: (renReq.id ?: 0L),
                    supervisorName = supervisorName,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = kodeUnitVal,
                    operator = renReq.operator ?: "",
                    sapCode = renReq.sap_code ?: "",
                    namaAktivitas = renReq.nama_aktivitas ?: "",
                    satuanAktivitas = renReq.satuan_aktivitas ?: "m3",
                    kodeLokasi = renReq.kode_lokasi ?: "",
                    shiftKerja = renReq.shift_kerja ?: "SIANG",
                    keterangan = renReq.keterangan ?: "",
                    nomorSpk = renReq.nomor_spk ?: "",
                    statusSpk = renReq.status_spk ?: "TERBIT_SPK",
                    statusTahap = renReq.status_tahap ?: "RENCANA",
                    isRealized = renReq.is_realized ?: false,
                    isSynced = true,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    tanggalFormat = tanggal
                )
                if (existing != null) {
                    rencanaDao.updateRencana(entity)
                } else {
                    rencanaDao.insertRencana(entity)
                }
                rencanaUpsertedCount++
            }

            // 3. Process Riwayat Aktivitas -> Insert / Upsert into activity_reports dengan isSynced = true
            val incomingReports = payload.resolvedRiwayatAktivitas
            var reportsUpsertedCount = 0
            for (repReq in incomingReports) {
                val unitCodeVal = repReq.kode_unit ?: repReq.unit_code ?: continue
                val hmAkhirVal = repReq.hm_akhir ?: 0.0
                val reportDateVal = repReq.tanggal ?: repReq.report_date ?: tanggal
                val clientUuidVal = repReq.client_uuid ?: repReq.client_transaction_id ?: "SRV-${supervisorName}-${unitCodeVal}-${hmAkhirVal}-${reportDateVal}"

                val existingByUuid = reportDao.getReportByUuid(clientUuidVal)
                val existingByMatch = if (existingByUuid == null) reportDao.findExistingReport(supervisorName, unitCodeVal, hmAkhirVal) else null
                val existing = existingByUuid ?: existingByMatch

                val entity = ActivityReportEntity(
                    id = existing?.id ?: 0L,
                    clientUuid = clientUuidVal,
                    reportDate = reportDateVal,
                    supervisorName = supervisorName,
                    unitCode = unitCodeVal,
                    unitType = repReq.jenis_unit ?: repReq.unit_type ?: "EXCAVATOR",
                    activityName = repReq.nama_aktivitas ?: repReq.activity_name ?: "",
                    sapCode = repReq.kode_sap ?: repReq.sap_code ?: "",
                    unitOfMeasure = repReq.satuan ?: repReq.unit_of_measure ?: "m3",
                    operatorName = repReq.nama_operator ?: repReq.operator_name ?: "",
                    workLocation = repReq.lokasi_kerja ?: repReq.work_location ?: "",
                    shift = repReq.shift ?: "SIANG",
                    workDurationHours = repReq.durasi_jam_kerja ?: repReq.work_duration_hours ?: 7.0,
                    hmAwal = repReq.hm_awal ?: 0.0,
                    hmAkhir = hmAkhirVal,
                    hmDailyRunning = repReq.hm_harian_berjalan ?: (hmAkhirVal - (repReq.hm_awal ?: 0.0)),
                    workOutput = repReq.hasil_kerja ?: repReq.work_output ?: 0.0,
                    notes = repReq.keterangan ?: repReq.notes ?: "",
                    unitStatus = repReq.status_unit ?: repReq.unit_status ?: "OPERASI",
                    isRefueled = repReq.is_isi_solar ?: repReq.is_refueled ?: false,
                    fuelLiters = repReq.jumlah_liter_solar ?: repReq.fuel_liters ?: 0.0,
                    stikSolarAwal = repReq.stik_solar_awal ?: 0.0,
                    stikSolarAkhir = repReq.stik_solar_akhir ?: 0.0,
                    pemakaianSolar = repReq.pemakaian_solar ?: 0.0,
                    kendalaId = repReq.kendala_id,
                    kendalaNama = repReq.kendala_nama,
                    catatanKendala = repReq.catatan_kendala,
                    daftarKendalaJson = if (repReq.daftar_kendala != null) KendalaJsonConverter.toJson(repReq.daftar_kendala) else null,
                    photoPath = repReq.photo_path,
                    photoBase64 = repReq.foto_bukti ?: repReq.photo_base64,
                    photoSizeKb = repReq.photo_size_kb,
                    photoDimensions = repReq.photo_dimensions,
                    isSynced = true, // Flagged is_synced = true agar tidak dianggap tertunda sync
                    isHmAwalCorrected = repReq.is_hm_awal_corrected ?: false,
                    alasanKoreksiHm = repReq.alasan_koreksi_hm,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    syncedAt = System.currentTimeMillis()
                )
                if (existing != null) {
                    reportDao.updateReport(entity)
                } else {
                    reportDao.insertReport(entity)
                }
                reportsUpsertedCount++
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            val summary = "$rencanaUpsertedCount Rencana Aktif, $reportsUpsertedCount Riwayat disinkronkan"
            addLog("GET /api/mobile/bootstrap-data", "GET", "200 OK", summary, true)
            Result.success("$message ($summary)")
        } catch (e: Exception) {
            addLog("GET /api/mobile/bootstrap-data", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun submitRencanaKerja(rencana: RencanaKerjaEntity): Result<RencanaKerjaEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = rencanaDao.insertRencana(rencana.copy(isSynced = false))
            val savedRencana = rencana.copy(id = insertedId)

            val apiReq = PostRencanaKerjaRequest(
                id = insertedId,
                status_unit = savedRencana.statusUnit,
                kode_unit = savedRencana.kodeUnit,
                operator = savedRencana.operator,
                sap_code = savedRencana.sapCode,
                nama_aktivitas = savedRencana.namaAktivitas,
                satuan_aktivitas = savedRencana.satuanAktivitas,
                kode_lokasi = savedRencana.kodeLokasi,
                shift_kerja = savedRencana.shiftKerja,
                keterangan = savedRencana.keterangan,
                nomor_spk = savedRencana.nomorSpk,
                status_spk = savedRencana.statusSpk,
                status_tahap = savedRencana.statusTahap,
                is_realized = savedRencana.isRealized,
                nama_pengawas = savedRencana.supervisorName
            )

            try {
                val respMsg = apiClientManager.postRencanaKerja(apiReq)
                rencanaDao.markAsSynced(insertedId)
                addLog("POST /api/rencana-kerja", "POST", "200 OK", "Rencana Unit ${savedRencana.kodeUnit} [${savedRencana.statusUnit}]", true)
                _lastSyncTimeMillis.value = System.currentTimeMillis()
                Result.success(savedRencana.copy(isSynced = true))
            } catch (netEx: Exception) {
                addLog("POST /api/rencana-kerja", "POST", "OFFLINE/PENDING", "Unit ${savedRencana.kodeUnit} disimpan lokal", false)
                Result.success(savedRencana.copy(isSynced = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRencanaSpk(planId: Long, spk: String, status: String = "SPK_TERBIT") = withContext(Dispatchers.IO) {
        rencanaDao.updateSpk(planId, spk, status)
    }

    suspend fun updateSpkStatus(kodeUnit: String, nomorSpk: String, statusSpk: String = "SPK_TERBIT") = withContext(Dispatchers.IO) {
        rencanaDao.updateSpkStatus(kodeUnit, nomorSpk, statusSpk)
    }

    suspend fun updateRencanaSpkByUnitOrId(planId: Long, unitKode: String, spk: String, status: String = "SPK_TERBIT"): RencanaKerjaEntity? = withContext(Dispatchers.IO) {
        if (planId > 0) {
            rencanaDao.updateSpk(planId, spk, status)
            val byId = rencanaDao.getRencanaById(planId)
            if (byId != null) return@withContext byId
        }
        if (unitKode.isNotBlank()) {
            rencanaDao.updateSpkByUnit(unitKode, spk, status)
            return@withContext rencanaDao.getRencanaByUnit(unitKode)
        }
        null
    }

    /**
     * Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * Mengganti seluruh daftar rencana kerja aktif di Room SQLite dengan data terbaru dari server.
     */
    suspend fun fetchAndReplaceActiveRencana(supervisorName: String): Result<Pair<List<RencanaKerjaEntity>, String>> = withContext(Dispatchers.IO) {
        try {
            val (incomingRequests, msg) = apiClientManager.fetchActiveRencanaKerja(supervisorName)
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val newEntities = incomingRequests.mapIndexed { index, renReq ->
                val kodeUnitVal = renReq.kode_unit?.trim()?.ifBlank { "UNIT-${index + 1}" } ?: "UNIT-${index + 1}"
                val spkNum = renReq.nomor_spk?.trim() ?: ""
                val spkStatus = when {
                    !renReq.status_spk.isNullOrBlank() -> renReq.status_spk
                    spkNum.isNotBlank() && spkNum != "SPK Menyusul" && spkNum != "-" -> "SPK_TERBIT"
                    else -> "MENUNGGU"
                }

                RencanaKerjaEntity(
                    id = if ((renReq.id ?: 0L) > 0) renReq.id!! else 0L,
                    supervisorName = supervisorName,
                    statusUnit = renReq.status_unit ?: "OPERASI",
                    kodeUnit = kodeUnitVal,
                    operator = renReq.operator ?: "",
                    sapCode = renReq.sap_code ?: "",
                    namaAktivitas = renReq.nama_aktivitas ?: "",
                    satuanAktivitas = renReq.satuan_aktivitas ?: "m3",
                    kodeLokasi = renReq.kode_lokasi ?: "",
                    shiftKerja = renReq.shift_kerja ?: "SIANG",
                    keterangan = renReq.keterangan ?: "",
                    nomorSpk = spkNum,
                    statusSpk = spkStatus,
                    statusTahap = renReq.status_tahap ?: "RENCANA",
                    isRealized = renReq.is_realized ?: false,
                    isSynced = true,
                    createdAt = System.currentTimeMillis() - (index * 1000L),
                    tanggalFormat = today
                )
            }

            if (newEntities.isNotEmpty()) {
                // Ganti seluruh daftar rencana kerja di layar dengan data terbaru dari server
                rencanaDao.clearActiveRencanaForSupervisor(supervisorName)
                rencanaDao.insertRencanaList(newEntities)
            }

            _lastSyncTimeMillis.value = System.currentTimeMillis()
            addLog("GET /api/mobile/rencana-kerja/active", "GET", "200 OK", "${newEntities.size} Rencana Kerja Aktif", true)
            Result.success(Pair(newEntities, msg))
        } catch (e: Exception) {
            addLog("GET /api/mobile/rencana-kerja/active", "GET", "ERROR", e.localizedMessage ?: "Gagal", false)
            Result.failure(e)
        }
    }

    suspend fun markRencanaRealized(planId: Long) = withContext(Dispatchers.IO) {
        rencanaDao.markAsRealized(planId)
        CompanyServerSimulator.markRencanaRealized(planId)
    }

    suspend fun submitHasilAktivitas(hasil: HasilInputAktivitasEntity): Result<HasilInputAktivitasEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = hasilDao.insertHasil(hasil.copy(isSynced = false))
            val savedHasil = hasil.copy(id = insertedId)

            // Update unit HM in local DB
            unitDao.updateUnitHm(savedHasil.kodeUnit, savedHasil.hmAkhir)

            // Mark Rencana Kerja as realized so it disappears from active dropdown immediately
            if (savedHasil.rencanaId > 0) {
                rencanaDao.markAsRealized(savedHasil.rencanaId)
                CompanyServerSimulator.markRencanaRealized(savedHasil.rencanaId)
            }

            val kendalaPayloads = KendalaJsonConverter.fromJson(savedHasil.daftarKendalaJson)

            // Also mirror into ActivityReportEntity for historical compatibility and analytics
            val reportMirror = ActivityReportEntity(
                reportDate = savedHasil.tanggalFormat.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                supervisorName = savedHasil.supervisorName,
                unitCode = savedHasil.kodeUnit,
                unitType = "",
                activityName = savedHasil.activityName,
                sapCode = savedHasil.sapCode,
                unitOfMeasure = savedHasil.satuan,
                operatorName = savedHasil.operator,
                workLocation = savedHasil.kodeLokasi,
                shift = savedHasil.shift,
                workDurationHours = savedHasil.jamKerja,
                hmAwal = savedHasil.hmAwal,
                hmAkhir = savedHasil.hmAkhir,
                hmDailyRunning = savedHasil.hmHarianBerjalan,
                workOutput = savedHasil.hasilKerja,
                notes = savedHasil.keterangan,
                unitStatus = savedHasil.unitStatus,
                isRefueled = savedHasil.isIsiSolar,
                fuelLiters = savedHasil.jumlahLiterSolar,
                stikSolarAwal = savedHasil.stikSolarAwal,
                stikSolarAkhir = savedHasil.stikSolarAkhir,
                pemakaianSolar = savedHasil.pemakaianSolar,
                kendalaId = savedHasil.kendalaId,
                kendalaNama = savedHasil.kendalaNama,
                catatanKendala = savedHasil.catatanKendala,
                daftarKendalaJson = savedHasil.daftarKendalaJson,
                photoPath = savedHasil.photoPath,
                photoBase64 = savedHasil.photoBase64,
                photoSizeKb = savedHasil.photoSizeKb,
                photoDimensions = savedHasil.photoDimensions,
                isHmAwalCorrected = savedHasil.isHmAwalCorrected,
                alasanKoreksiHm = savedHasil.alasanKoreksiHm,
                isSynced = false
            )
            val mirrorReportId = reportDao.insertReport(reportMirror)
            savedHasil.hmAkhir?.let { hmAkhir ->
                unitDao.updateUnitHm(savedHasil.kodeUnit, hmAkhir)
            }

            val spkClean = if (savedHasil.nomorSpk.isNotBlank() &&
                savedHasil.nomorSpk != "SPK Menyusul" &&
                savedHasil.nomorSpk != "Belum Terbit (Menunggu Admin)" &&
                savedHasil.nomorSpk != "-") savedHasil.nomorSpk else null

            val apiReq = PostHasilAktivitasRequest(
                id = insertedId,
                rencana_id = savedHasil.rencanaId,
                id_rencana_kerja = savedHasil.rencanaId,
                nomor_spk = spkClean,
                jam_kerja = savedHasil.jamKerja,
                hm_awal = savedHasil.hmAwal,
                hm_akhir = savedHasil.hmAkhir,
                hm_harian_berjalan = savedHasil.hmHarianBerjalan,
                is_isi_solar = savedHasil.isIsiSolar,
                jumlah_liter_solar = savedHasil.jumlahLiterSolar,
                stik_solar_awal = savedHasil.stikSolarAwal,
                stik_solar_akhir = savedHasil.stikSolarAkhir,
                pemakaian_solar = savedHasil.pemakaianSolar,
                kendala_id = savedHasil.kendalaId,
                kendala_nama = savedHasil.kendalaNama,
                catatan_kendala = savedHasil.catatanKendala,
                daftar_kendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                hasil_kerja = savedHasil.hasilKerja,
                keterangan = savedHasil.keterangan,
                nama_pengawas = savedHasil.supervisorName,
                kode_unit = savedHasil.kodeUnit,
                foto_bukti = savedHasil.photoBase64,
                is_hm_awal_corrected = savedHasil.isHmAwalCorrected,
                alasan_koreksi_hm = savedHasil.alasanKoreksiHm,
                client_transaction_id = savedHasil.clientTransactionId
            )

            val apiActivityReq = PostActivityReportRequest.create(
                clientUuid = reportMirror.clientUuid,
                reportDate = reportMirror.reportDate,
                supervisorName = reportMirror.supervisorName,
                unitCode = reportMirror.unitCode,
                unitType = reportMirror.unitType,
                activityName = reportMirror.activityName,
                sapCode = reportMirror.sapCode,
                unitOfMeasure = reportMirror.unitOfMeasure,
                operatorName = reportMirror.operatorName,
                workLocation = reportMirror.workLocation,
                shift = reportMirror.shift,
                workDurationHours = reportMirror.workDurationHours,
                hmAwal = reportMirror.hmAwal,
                hmAkhir = reportMirror.hmAkhir,
                hmDailyRunning = reportMirror.hmDailyRunning,
                workOutput = reportMirror.workOutput,
                notes = reportMirror.notes,
                unitStatus = reportMirror.unitStatus,
                isRefueled = reportMirror.isRefueled,
                fuelLiters = reportMirror.fuelLiters,
                stikSolarAwal = reportMirror.stikSolarAwal,
                stikSolarAkhir = reportMirror.stikSolarAkhir,
                pemakaianSolar = reportMirror.pemakaianSolar,
                kendalaId = reportMirror.kendalaId,
                kendalaNama = reportMirror.kendalaNama,
                catatanKendala = reportMirror.catatanKendala,
                daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                photoBase64 = reportMirror.photoBase64,
                photoSizeKb = reportMirror.photoSizeKb,
                photoDimensions = reportMirror.photoDimensions,
                photoPath = reportMirror.photoPath,
                isHmAwalCorrected = savedHasil.isHmAwalCorrected,
                alasanKoreksiHm = savedHasil.alasanKoreksiHm,
                clientTransactionId = savedHasil.clientTransactionId,
                idRencanaKerja = savedHasil.rencanaId,
                nomorSpk = spkClean
            )

            try {
                try { apiClientManager.postActivityReport(apiActivityReq) } catch (_: Exception) {}
                val respMsg = apiClientManager.postHasilAktivitas(apiReq)
                val syncTimestamp = System.currentTimeMillis()
                // Update langsung SQLite/Room lokal: is_synced = true dan status_sync = 'SYNCED'
                hasilDao.markAsSynced(insertedId, syncTimestamp)
                reportDao.markReportAsSynced(mirrorReportId, syncTimestamp)
                reportDao.markReportAsSyncedByUnitAndHm(savedHasil.kodeUnit, savedHasil.hmAkhir, savedHasil.supervisorName, syncTimestamp)
                draftDao.deleteDraftByRencanaId(savedHasil.rencanaId)
                _lastSyncTimeMillis.value = syncTimestamp
                val kendalaTag = if (kendalaPayloads.isNotEmpty()) " [Kendala: ${kendalaPayloads.size} item]" else ""
                addLog("POST /api/aktivitas-unit", "POST", "200 OK", "Realisasi Unit ${savedHasil.kodeUnit} (HM ${savedHasil.hmAkhir})$kendalaTag", true)
                Result.success(savedHasil.copy(isSynced = true, statusKirim = "SYNCED", syncedAt = syncTimestamp))
            } catch (netEx: Exception) {
                addLog("POST /api/aktivitas-unit", "POST", "OFFLINE/PENDING", "Realisasi Unit ${savedHasil.kodeUnit} disimpan lokal", false)
                Result.success(savedHasil.copy(isSynced = false, statusKirim = "PENDING"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitReport(report: ActivityReportEntity): Result<ActivityReportEntity> = withContext(Dispatchers.IO) {
        try {
            val insertedId = reportDao.insertReport(report.copy(isSynced = false))
            val savedReport = report.copy(id = insertedId)
            val kendalaPayloads = KendalaJsonConverter.fromJson(savedReport.daftarKendalaJson)

            val apiReq = PostActivityReportRequest.create(
                clientUuid = savedReport.clientUuid,
                reportDate = savedReport.reportDate,
                supervisorName = savedReport.supervisorName,
                unitCode = savedReport.unitCode,
                unitType = savedReport.unitType,
                activityName = savedReport.activityName,
                sapCode = savedReport.sapCode,
                unitOfMeasure = savedReport.unitOfMeasure,
                operatorName = savedReport.operatorName,
                workLocation = savedReport.workLocation,
                shift = savedReport.shift,
                workDurationHours = savedReport.workDurationHours,
                hmAwal = savedReport.hmAwal,
                hmAkhir = savedReport.hmAkhir,
                hmDailyRunning = savedReport.hmDailyRunning,
                workOutput = savedReport.workOutput,
                notes = savedReport.notes,
                unitStatus = savedReport.unitStatus,
                isRefueled = savedReport.isRefueled,
                fuelLiters = savedReport.fuelLiters,
                kendalaId = savedReport.kendalaId,
                kendalaNama = savedReport.kendalaNama,
                catatanKendala = savedReport.catatanKendala,
                daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                photoBase64 = savedReport.photoBase64,
                photoSizeKb = savedReport.photoSizeKb,
                photoDimensions = savedReport.photoDimensions,
                photoPath = savedReport.photoPath,
                isHmAwalCorrected = savedReport.isHmAwalCorrected,
                alasanKoreksiHm = savedReport.alasanKoreksiHm
            )

            try {
                val responseMsg = apiClientManager.postActivityReport(apiReq)
                reportDao.markReportAsSynced(insertedId)
                unitDao.updateUnitHm(savedReport.unitCode, savedReport.hmAkhir)

                val fuelTag = if (savedReport.isRefueled) " [Solar: ${savedReport.fuelLiters}L]" else ""
                addLog("POST /api/aktivitas-unit", "POST", "200 OK", "Unit ${savedReport.unitCode} [${savedReport.unitStatus}]$fuelTag (HM ${savedReport.hmAkhir})", true)
                Result.success(savedReport.copy(isSynced = true, syncedAt = System.currentTimeMillis()))
            } catch (netEx: Exception) {
                unitDao.updateUnitHm(savedReport.unitCode, savedReport.hmAkhir)
                addLog("POST /api/aktivitas-unit", "POST", "OFFLINE/PENDING", "Unit ${savedReport.unitCode} [${savedReport.unitStatus}] disimpan di SQLite lokal", false)
                Result.success(savedReport.copy(isSynced = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingReports(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Anti-duplikat: Filter hanya record yang benar-benar memiliki isSynced == false
            val pending = reportDao.getUnsyncedReportsList().filter { !it.isSynced }
            if (pending.isEmpty()) {
                return@withContext Result.success(0)
            }

            var successCount = 0
            for (report in pending) {
                try {
                    val kendalaPayloads = KendalaJsonConverter.fromJson(report.daftarKendalaJson)
                    val apiReq = PostActivityReportRequest.create(
                        clientUuid = report.clientUuid,
                        reportDate = report.reportDate,
                        supervisorName = report.supervisorName,
                        unitCode = report.unitCode,
                        unitType = report.unitType,
                        activityName = report.activityName,
                        sapCode = report.sapCode,
                        unitOfMeasure = report.unitOfMeasure,
                        operatorName = report.operatorName,
                        workLocation = report.workLocation,
                        shift = report.shift,
                        workDurationHours = report.workDurationHours,
                        hmAwal = report.hmAwal,
                        hmAkhir = report.hmAkhir,
                        hmDailyRunning = report.hmDailyRunning,
                        workOutput = report.workOutput,
                        notes = report.notes,
                        unitStatus = report.unitStatus,
                        isRefueled = report.isRefueled,
                        fuelLiters = report.fuelLiters,
                        kendalaId = report.kendalaId,
                        kendalaNama = report.kendalaNama,
                        catatanKendala = report.catatanKendala,
                        daftarKendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        photoBase64 = report.photoBase64,
                        photoSizeKb = report.photoSizeKb,
                        photoDimensions = report.photoDimensions,
                        photoPath = report.photoPath,
                        isHmAwalCorrected = report.isHmAwalCorrected,
                        alasanKoreksiHm = report.alasanKoreksiHm
                    )
                    apiClientManager.postActivityReport(apiReq)
                    val now = System.currentTimeMillis()
                    reportDao.markReportAsSynced(report.id, now)
                    hasilDao.markHasilAsSyncedByUnitAndHm(report.unitCode, report.hmAkhir, report.supervisorName, now)
                    unitDao.updateUnitHm(report.unitCode, report.hmAkhir)
                    successCount++
                } catch (_: Exception) {}
            }

            if (successCount > 0) {
                addLog("POST /api/aktivitas-unit (BATCH)", "POST", "SYNC COMPLETED", "Sinkron $successCount dari ${pending.size} laporan", true)
                _lastSyncTimeMillis.value = System.currentTimeMillis()
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingOperationalData(): Result<Int> = withContext(Dispatchers.IO) {
        var totalSynced = 0
        try {
            // 1. Sync pending rencana: Filter hanya record yang benar-benar is_synced == false
            val pendingRencana = rencanaDao.getUnsyncedRencanaList().filter { !it.isSynced }
            for (rencana in pendingRencana) {
                try {
                    val req = PostRencanaKerjaRequest(
                        id = rencana.id,
                        status_unit = rencana.statusUnit,
                        kode_unit = rencana.kodeUnit,
                        operator = rencana.operator,
                        kode_lokasi = rencana.kodeLokasi,
                        shift_kerja = rencana.shiftKerja,
                        keterangan = rencana.keterangan,
                        nomor_spk = rencana.nomorSpk,
                        status_spk = rencana.statusSpk,
                        nama_pengawas = rencana.supervisorName
                    )
                    apiClientManager.postRencanaKerja(req)
                    rencanaDao.markAsSynced(rencana.id)
                    totalSynced++
                } catch (_: Exception) {}
            }

            // 2. Sync pending hasil aktivitas: Filter hanya record yang benar-benar is_synced == false atau status_kirim == 'PENDING'
            val pendingHasil = hasilDao.getUnsyncedHasilList().filter { !it.isSynced || it.statusKirim == "PENDING" }
            for (hasil in pendingHasil) {
                try {
                    val kendalaPayloads = KendalaJsonConverter.fromJson(hasil.daftarKendalaJson)
                    val req = PostHasilAktivitasRequest(
                        id = hasil.id,
                        rencana_id = hasil.rencanaId,
                        nomor_spk = hasil.nomorSpk,
                        jam_kerja = hasil.jamKerja,
                        hm_awal = hasil.hmAwal,
                        hm_akhir = hasil.hmAkhir,
                        hm_harian_berjalan = hasil.hmHarianBerjalan,
                        is_isi_solar = hasil.isIsiSolar,
                        jumlah_liter_solar = hasil.jumlahLiterSolar,
                        kendala_id = hasil.kendalaId,
                        kendala_nama = hasil.kendalaNama,
                        catatan_kendala = hasil.catatanKendala,
                        daftar_kendala = if (kendalaPayloads.isNotEmpty()) kendalaPayloads else null,
                        hasil_kerja = hasil.hasilKerja,
                        keterangan = hasil.keterangan,
                        nama_pengawas = hasil.supervisorName,
                        kode_unit = hasil.kodeUnit,
                        foto_bukti = hasil.photoBase64,
                        is_hm_awal_corrected = hasil.isHmAwalCorrected,
                        alasan_koreksi_hm = hasil.alasanKoreksiHm
                    )
                    apiClientManager.postHasilAktivitas(req)
                    val now = System.currentTimeMillis()
                    hasilDao.markAsSynced(hasil.id, now)
                    reportDao.markReportAsSyncedByUnitAndHm(hasil.kodeUnit, hasil.hmAkhir, hasil.supervisorName, now)
                    draftDao.deleteDraftByRencanaId(hasil.rencanaId)
                    totalSynced++
                } catch (_: Exception) {}
            }

            // 3. Sync pending reports (ActivityReportEntity) yang belum tersinkron
            val reportsResult = syncAllPendingReports()
            totalSynced += reportsResult.getOrDefault(0)

            if (totalSynced > 0) {
                _lastSyncTimeMillis.value = System.currentTimeMillis()
                addLog("BATCH QUEUE SYNC", "SYNC", "SUCCESS", "Tersinkron total $totalSynced entri operasional", true)
            }
            Result.success(totalSynced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(url: String): Pair<Boolean, String> {
        val result = apiClientManager.testConnection(url)
        addLog("TEST $url", "PING", if (result.first) "CONNECTED" else "FAILED", result.second, result.first)
        return result
    }

    suspend fun deleteReport(reportId: Long) = withContext(Dispatchers.IO) {
        reportDao.deleteReport(reportId)
    }

    suspend fun deleteRencana(rencanaId: Long) = withContext(Dispatchers.IO) {
        rencanaDao.deleteRencana(rencanaId)
    }

    suspend fun deleteHasil(hasilId: Long) = withContext(Dispatchers.IO) {
        hasilDao.deleteHasil(hasilId)
    }

    suspend fun getDraftForRencana(rencanaId: Long): com.example.data.local.entity.DraftRealisasiEntity? = withContext(Dispatchers.IO) {
        draftDao.getDraftByRencanaId(rencanaId)
    }

    suspend fun saveDraftForRencana(draft: com.example.data.local.entity.DraftRealisasiEntity) = withContext(Dispatchers.IO) {
        draftDao.insertOrUpdateDraft(draft)
    }

    suspend fun deleteDraftForRencana(rencanaId: Long) = withContext(Dispatchers.IO) {
        draftDao.deleteDraftByRencanaId(rencanaId)
    }
}
