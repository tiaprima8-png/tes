package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class PostActivityReportRequest(
    // Indonesian keys expected by https://1-portalce.ai.studio/api/aktivitas-unit
    @Json(name = "tanggal") val tanggal: String? = null,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "jenis_unit") val jenis_unit: String? = null,
    @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @Json(name = "kode_sap") val kode_sap: String? = null,
    @Json(name = "satuan") val satuan: String? = null,
    @Json(name = "nama_operator") val nama_operator: String? = null,
    @Json(name = "lokasi_kerja") val lokasi_kerja: String? = null,
    @Json(name = "shift") val shift: String? = null,
    @Json(name = "durasi_jam_kerja") val durasi_jam_kerja: Double? = null,
    @Json(name = "hm_awal") val hm_awal: Double? = null,
    @Json(name = "hm_akhir") val hm_akhir: Double? = null,
    @Json(name = "hm_harian_berjalan") val hm_harian_berjalan: Double? = null,
    @Json(name = "hasil_kerja") val hasil_kerja: Double? = null,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "status_unit") val status_unit: String? = "OPERASI", // "OPERASI" | "STANDBY" | "BREAKDOWN"
    @Json(name = "is_isi_solar") val is_isi_solar: Boolean? = false,
    @Json(name = "jumlah_liter_solar") val jumlah_liter_solar: Double? = 0.0,
    @Json(name = "stik_solar_awal") val stik_solar_awal: Double? = null,
    @Json(name = "stik_solar_akhir") val stik_solar_akhir: Double? = null,
    @Json(name = "pemakaian_solar") val pemakaian_solar: Double? = null,
    @Json(name = "kendala_id") val kendala_id: String? = null,
    @Json(name = "kendala_nama") val kendala_nama: String? = null,
    @Json(name = "catatan_kendala") val catatan_kendala: String? = null,
    @Json(name = "daftar_kendala") val daftar_kendala: List<KendalaItemPayload>? = null,
    @Json(name = "foto_bukti") val foto_bukti: String? = null,
    @Json(name = "foto") val foto: String? = null,
    @Json(name = "photo_base64") val photo_base64: String? = null,
    @Json(name = "photo_path") val photo_path: String? = null,
    @Json(name = "photo_size_kb") val photo_size_kb: Long? = null,
    @Json(name = "photo_dimensions") val photo_dimensions: String? = null,
    // Audit log koreksi HM Awal manual
    @Json(name = "is_hm_awal_corrected") val is_hm_awal_corrected: Boolean? = false,
    @Json(name = "alasan_koreksi_hm") val alasan_koreksi_hm: String? = null,
    @Json(name = "client_transaction_id") val client_transaction_id: String? = null,
    @Json(name = "id_rencana_kerja") val id_rencana_kerja: Long? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,

    // Dual compatibility with standard camelCase/English keys
    @Json(name = "client_uuid") val client_uuid: String? = null,
    @Json(name = "report_date") val report_date: String? = null,
    @Json(name = "supervisor_name") val supervisor_name: String? = null,
    @Json(name = "unit_code") val unit_code: String? = null,
    @Json(name = "unit_type") val unit_type: String? = null,
    @Json(name = "unit_status") val unit_status: String? = null,
    @Json(name = "is_refueled") val is_refueled: Boolean? = null,
    @Json(name = "fuel_liters") val fuel_liters: Double? = null,
    @Json(name = "activity_name") val activity_name: String? = null,
    @Json(name = "sap_code") val sap_code: String? = null,
    @Json(name = "unit_of_measure") val unit_of_measure: String? = null,
    @Json(name = "operator_name") val operator_name: String? = null,
    @Json(name = "work_location") val work_location: String? = null,
    @Json(name = "work_duration_hours") val work_duration_hours: Double? = null,
    @Json(name = "work_output") val work_output: Double? = null,
    @Json(name = "notes") val notes: String? = null
) {
    companion object {
        fun create(
            clientUuid: String,
            reportDate: String,
            supervisorName: String,
            unitCode: String,
            unitType: String,
            activityName: String,
            sapCode: String,
            unitOfMeasure: String,
            operatorName: String,
            workLocation: String,
            shift: String,
            workDurationHours: Double,
            hmAwal: Double,
            hmAkhir: Double,
            hmDailyRunning: Double,
            workOutput: Double,
            notes: String,
            unitStatus: String = "OPERASI",
            isRefueled: Boolean = false,
            fuelLiters: Double = 0.0,
            stikSolarAwal: Double? = null,
            stikSolarAkhir: Double? = null,
            pemakaianSolar: Double? = null,
            kendalaId: String? = null,
            kendalaNama: String? = null,
            catatanKendala: String? = null,
            daftarKendala: List<KendalaItemPayload>? = null,
            photoBase64: String? = null,
            photoSizeKb: Long? = null,
            photoDimensions: String? = null,
            photoPath: String? = null,
            isHmAwalCorrected: Boolean = false,
            alasanKoreksiHm: String? = null,
            clientTransactionId: String? = null,
            idRencanaKerja: Long? = null,
            nomorSpk: String? = null
        ): PostActivityReportRequest {
            return PostActivityReportRequest(
                // Indonesian exact fields for 1-portalce.ai.studio
                tanggal = reportDate,
                nama_pengawas = supervisorName,
                kode_unit = unitCode,
                jenis_unit = unitType,
                nama_aktivitas = activityName,
                kode_sap = sapCode,
                satuan = unitOfMeasure,
                nama_operator = operatorName,
                lokasi_kerja = workLocation,
                shift = shift,
                durasi_jam_kerja = workDurationHours,
                hm_awal = hmAwal,
                hm_akhir = hmAkhir,
                hm_harian_berjalan = hmDailyRunning,
                hasil_kerja = workOutput,
                keterangan = notes,
                status_unit = unitStatus,
                is_isi_solar = isRefueled,
                jumlah_liter_solar = fuelLiters,
                stik_solar_awal = stikSolarAwal,
                stik_solar_akhir = stikSolarAkhir,
                pemakaian_solar = pemakaianSolar,
                kendala_id = kendalaId,
                kendala_nama = kendalaNama,
                catatan_kendala = catatanKendala,
                daftar_kendala = daftarKendala,
                foto_bukti = photoBase64,
                foto = photoBase64,
                photo_base64 = photoBase64,
                photo_path = photoPath,
                photo_size_kb = photoSizeKb,
                photo_dimensions = photoDimensions,
                is_hm_awal_corrected = isHmAwalCorrected,
                alasan_koreksi_hm = alasanKoreksiHm,
                client_transaction_id = clientTransactionId,
                id_rencana_kerja = idRencanaKerja,
                nomor_spk = nomorSpk,

                // Fallbacks
                client_uuid = clientUuid,
                report_date = reportDate,
                supervisor_name = supervisorName,
                unit_code = unitCode,
                unit_type = unitType,
                unit_status = unitStatus,
                is_refueled = isRefueled,
                fuel_liters = fuelLiters,
                activity_name = activityName,
                sap_code = sapCode,
                unit_of_measure = unitOfMeasure,
                operator_name = operatorName,
                work_location = workLocation,
                work_duration_hours = workDurationHours,
                work_output = workOutput,
                notes = notes
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class KendalaItemPayload(
    @Json(name = "id_kendala") val id_kendala: String? = null,
    @Json(name = "nama_kendala") val nama_kendala: String? = null,
    @Json(name = "waktu_mulai") val waktu_mulai: String? = null,
    @Json(name = "waktu_selesai") val waktu_selesai: String? = null,
    @Json(name = "durasi_menit") val durasi_menit: Int? = null
)

@JsonClass(generateAdapter = true)
data class KendalaPayload(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "id_kendala") val id_kendala: String? = null,
    @Json(name = "kode_kendala") val kode_kendala: String? = null,
    @Json(name = "kode") val kode: String? = null,
    @Json(name = "nama_kendala") val nama_kendala: String? = null,
    @Json(name = "nama") val nama: String? = null,
    @Json(name = "kategori") val kategori: String? = null,
    @Json(name = "deskripsi") val deskripsi: String? = null,
    @Json(name = "keterangan") val keterangan: String? = null
) {
    fun resolveKode(index: Int): String {
        val candidate = kode_kendala ?: kode ?: id_kendala ?: (id?.toString())
        return if (!candidate.isNullOrBlank()) {
            candidate
        } else {
            String.format(java.util.Locale.US, "KND-%02d", index + 1)
        }
    }

    val resolvedNama: String
        get() = (nama_kendala ?: nama ?: deskripsi ?: keterangan)?.takeIf { it.isNotBlank() } ?: "Kendala Lapangan"

    val resolvedKategori: String
        get() = kategori?.takeIf { it.isNotBlank() } ?: "OPERASIONAL"

    val resolvedDeskripsi: String
        get() = (deskripsi ?: keterangan) ?: ""
}

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val status: String? = "success",
    val message: String? = "",
    val data: T? = null,
    val timestamp: Long? = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class MasterDataPayload(
    val units: List<UnitPayload>? = emptyList(),
    // Matches 1-portalce.ai.studio "aktivitas"
    val aktivitas: List<ActivityPayload>? = emptyList(),
    val activities: List<ActivityPayload>? = emptyList(),
    val operators: List<OperatorPayload>? = emptyList(),
    val lokasi: List<LokasiPayload>? = emptyList(),
    val locations: List<LokasiPayload>? = emptyList(),
    val kendala: List<KendalaPayload>? = emptyList()
) {
    val resolvedActivities: List<ActivityPayload>
        get() {
            if (!aktivitas.isNullOrEmpty()) return aktivitas
            if (!activities.isNullOrEmpty()) return activities
            return emptyList()
        }

    val resolvedLokasi: List<LokasiPayload>
        get() {
            if (!lokasi.isNullOrEmpty()) return lokasi
            if (!locations.isNullOrEmpty()) return locations
            return emptyList()
        }
}

@JsonClass(generateAdapter = true)
data class BootstrapDataPayload(
    @Json(name = "master_data") val master_data: MasterDataPayload? = null,
    @Json(name = "masterData") val masterData: MasterDataPayload? = null,
    @Json(name = "rencana_aktif") val rencana_aktif: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "rencanaAktif") val rencanaAktif: List<PostRencanaKerjaRequest>? = emptyList(),
    @Json(name = "riwayat_aktivitas") val riwayat_aktivitas: List<PostActivityReportRequest>? = emptyList(),
    @Json(name = "riwayatAktivitas") val riwayatAktivitas: List<PostActivityReportRequest>? = emptyList(),

    // Flat fallbacks if server returns master fields directly:
    val units: List<UnitPayload>? = emptyList(),
    val aktivitas: List<ActivityPayload>? = emptyList(),
    val activities: List<ActivityPayload>? = emptyList(),
    val operators: List<OperatorPayload>? = emptyList(),
    val lokasi: List<LokasiPayload>? = emptyList(),
    val kendala: List<KendalaPayload>? = emptyList()
) {
    val resolvedRencanaAktif: List<PostRencanaKerjaRequest>
        get() = if (!rencana_aktif.isNullOrEmpty()) rencana_aktif else (rencanaAktif ?: emptyList())

    val resolvedRiwayatAktivitas: List<PostActivityReportRequest>
        get() = if (!riwayat_aktivitas.isNullOrEmpty()) riwayat_aktivitas else (riwayatAktivitas ?: emptyList())

    val resolvedMasterData: MasterDataPayload
        get() = master_data ?: masterData ?: MasterDataPayload(
            units = units,
            aktivitas = aktivitas,
            activities = activities,
            operators = operators,
            lokasi = lokasi,
            kendala = kendala
        )
}

@JsonClass(generateAdapter = true)
data class UnitPayload(
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "unit_code") val unit_code: String? = null,
    @Json(name = "unitCode") val unitCode: String? = null,

    @Json(name = "jenis_unit") val jenis_unit: String? = null,
    @Json(name = "unit_type") val unit_type: String? = null,
    @Json(name = "unitType") val unitType: String? = null,

    @Json(name = "model_unit") val model_unit: String? = null,
    @Json(name = "brand_model") val brand_model: String? = null,
    @Json(name = "brandModel") val brandModel: String? = null,

    @Json(name = "hm_unit_terakhir_diinputkan") val hm_unit_terakhir_diinputkan: Double? = null,
    @Json(name = "last_hm") val last_hm: Double? = null,
    @Json(name = "lastHm") val lastHm: Double? = null,

    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "supervisor_assigned") val supervisor_assigned: String? = null,
    @Json(name = "supervisorAssigned") val supervisorAssigned: String? = null,

    @Json(name = "status") val status: String? = "OPERASIONAL",
    @Json(name = "hm_min_standar") val hm_min_standar: Double? = null,
    @Json(name = "hm_max_standar") val hm_max_standar: Double? = null,
    @Json(name = "hm_min") val hm_min: Double? = null,
    @Json(name = "hm_max") val hm_max: Double? = null,
    @Json(name = "min_standar") val min_standar: Double? = null,
    @Json(name = "max_standar") val max_standar: Double? = null,
    @Json(name = "hmMinStandar") val hmMinStandar: Double? = null,
    @Json(name = "hmMaxStandar") val hmMaxStandar: Double? = null
) {
    constructor(
        unitCode: String,
        unitType: String,
        brandModel: String,
        lastHm: Double,
        supervisorAssigned: String,
        status: String = "OPERASIONAL",
        hmMinStandar: Double = 6.0,
        hmMaxStandar: Double = 10.0
    ) : this(
        kode_unit = unitCode,
        unit_code = unitCode,
        unitCode = unitCode,
        jenis_unit = unitType,
        unit_type = unitType,
        unitType = unitType,
        model_unit = brandModel,
        brand_model = brandModel,
        brandModel = brandModel,
        hm_unit_terakhir_diinputkan = lastHm,
        last_hm = lastHm,
        lastHm = lastHm,
        nama_pengawas = supervisorAssigned,
        supervisor_assigned = supervisorAssigned,
        supervisorAssigned = supervisorAssigned,
        status = status,
        hm_min_standar = hmMinStandar,
        hm_max_standar = hmMaxStandar
    )

    val resolvedUnitCode: String get() = kode_unit ?: unit_code ?: unitCode ?: "UNKNOWN"
    val resolvedUnitType: String get() = jenis_unit ?: unit_type ?: unitType ?: "EXCAVATOR"
    val resolvedBrandModel: String get() = model_unit ?: brand_model ?: brandModel ?: ""
    val resolvedLastHm: Double get() = hm_unit_terakhir_diinputkan ?: last_hm ?: lastHm ?: 0.0
    val resolvedSupervisor: String get() = nama_pengawas ?: supervisor_assigned ?: supervisorAssigned ?: ""
    val resolvedStatus: String get() = status ?: "OPERASIONAL"
    val resolvedHmMinStandar: Double get() = hm_min_standar ?: hm_min ?: min_standar ?: hmMinStandar ?: 6.0
    val resolvedHmMaxStandar: Double get() = hm_max_standar ?: hm_max ?: max_standar ?: hmMaxStandar ?: 10.0
}

@JsonClass(generateAdapter = true)
data class ActivityPayload(
    @Json(name = "kode_sap") val kode_sap: String? = null,
    @Json(name = "sap_code") val sap_code: String? = null,
    @Json(name = "sapCode") val sapCode: String? = null,

    @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @Json(name = "activity_name") val activity_name: String? = null,
    @Json(name = "activityName") val activityName: String? = null,

    @Json(name = "satuan") val satuan: String? = null,
    @Json(name = "unit_of_measure") val unit_of_measure: String? = null,
    @Json(name = "unitOfMeasure") val unitOfMeasure: String? = null,

    @Json(name = "description") val description: String? = null,
    @Json(name = "keterangan") val keterangan: String? = null,

    @Json(name = "kategori") val kategori: String? = null,
    @Json(name = "category") val category: String? = "PRODUKSI",

    @Json(name = "jenis_unit") val jenis_unit: String? = null
) {
    constructor(
        sapCode: String,
        activityName: String,
        unitOfMeasure: String,
        description: String = "",
        category: String = "PRODUKSI"
    ) : this(
        kode_sap = sapCode,
        sap_code = sapCode,
        sapCode = sapCode,
        nama_aktivitas = activityName,
        activity_name = activityName,
        activityName = activityName,
        satuan = unitOfMeasure,
        unit_of_measure = unitOfMeasure,
        unitOfMeasure = unitOfMeasure,
        description = description,
        keterangan = description,
        kategori = category,
        category = category
    )

    val resolvedSapCode: String get() = kode_sap ?: sap_code ?: sapCode ?: "ACT-SAP-999"
    val resolvedActivityName: String get() = nama_aktivitas ?: activity_name ?: activityName ?: "Aktivitas Lapangan"
    val resolvedUnitOfMeasure: String get() = satuan ?: unit_of_measure ?: unitOfMeasure ?: "m3"
    val resolvedDescription: String get() = description ?: keterangan ?: ""
    val resolvedCategory: String get() = kategori ?: category ?: "PRODUKSI"
}

@JsonClass(generateAdapter = true)
data class OperatorPayload(
    @Json(name = "nama_operator") val nama_operator: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "nama") val nama: String? = null,

    @Json(name = "nik") val nik: String? = null,
    @Json(name = "badge_number") val badge_number: String? = null,
    @Json(name = "badgeNumber") val badgeNumber: String? = null,

    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "supervisor_assigned") val supervisor_assigned: String? = null,
    @Json(name = "supervisorAssigned") val supervisorAssigned: String? = null,

    @Json(name = "jabatan") val jabatan: String? = null,
    @Json(name = "skill_level") val skill_level: String? = "Class A",
    @Json(name = "skillLevel") val skillLevel: String? = null,

    @Json(name = "operator_id") val operator_id: String? = null,
    @Json(name = "operatorId") val operatorId: String? = null,

    @Json(name = "status") val status: String? = "AKTIF"
) {
    constructor(
        operatorId: String,
        name: String,
        badgeNumber: String,
        supervisorAssigned: String,
        skillLevel: String = "Class A",
        status: String = "AKTIF"
    ) : this(
        nama_operator = name,
        name = name,
        nama = name,
        nik = badgeNumber,
        badge_number = badgeNumber,
        badgeNumber = badgeNumber,
        nama_pengawas = supervisorAssigned,
        supervisor_assigned = supervisorAssigned,
        supervisorAssigned = supervisorAssigned,
        jabatan = skillLevel,
        skill_level = skillLevel,
        skillLevel = skillLevel,
        operator_id = operatorId,
        operatorId = operatorId,
        status = status
    )

    val resolvedOperatorId: String get() = operator_id ?: operatorId ?: nik ?: "OPR-000"
    val resolvedName: String get() = nama_operator ?: name ?: nama ?: "Operator"
    val resolvedBadgeNumber: String get() = nik ?: badge_number ?: badgeNumber ?: "NIK-000"
    val resolvedSupervisor: String get() = nama_pengawas ?: supervisor_assigned ?: supervisorAssigned ?: ""
    val resolvedSkillLevel: String get() = jabatan ?: skill_level ?: skillLevel ?: "Class A"
    val resolvedStatus: String get() = status ?: "AKTIF"
}

@JsonClass(generateAdapter = true)
data class LokasiPayload(
    @Json(name = "kode_lokasi") val kode_lokasi: String? = null,
    @Json(name = "kodeLokasi") val kodeLokasi: String? = null,
    @Json(name = "wilayah") val wilayah: String? = null,
    @Json(name = "luas_bruto") val luas_bruto: Double? = null,
    @Json(name = "luas_netto") val luas_netto: Double? = null,
    @Json(name = "keterangan") val keterangan: String? = null
) {
    constructor(
        kodeLokasi: String,
        wilayah: String,
        luasBruto: Double,
        luasNetto: Double,
        keterangan: String = ""
    ) : this(
        kode_lokasi = kodeLokasi,
        kodeLokasi = kodeLokasi,
        wilayah = wilayah,
        luas_bruto = luasBruto,
        luas_netto = luasNetto,
        keterangan = keterangan
    )

    val resolvedKode: String get() = kode_lokasi ?: kodeLokasi ?: "LOK-001"
    val resolvedWilayah: String get() = wilayah ?: "PG1"
    val resolvedLuasBruto: Double get() = luas_bruto ?: 0.0
    val resolvedLuasNetto: Double get() = luas_netto ?: 0.0
}

@JsonClass(generateAdapter = true)
data class PostRencanaKerjaRequest(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "status_unit") val status_unit: String? = "OPERASI",
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "operator") val operator: String? = null,
    @Json(name = "sap_code") val sap_code: String? = null,
    @Json(name = "nama_aktivitas") val nama_aktivitas: String? = null,
    @Json(name = "satuan_aktivitas") val satuan_aktivitas: String? = null,
    @Json(name = "kode_lokasi") val kode_lokasi: String? = null,
    @Json(name = "shift_kerja") val shift_kerja: String? = "SIANG",
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,
    @Json(name = "status_spk") val status_spk: String? = "MENUNGGU_SPK",
    @Json(name = "status_tahap") val status_tahap: String? = "RENCANA",
    @Json(name = "is_realized") val is_realized: Boolean? = false,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null
)

@JsonClass(generateAdapter = true)
data class PostHasilAktivitasRequest(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "rencana_id") val rencana_id: Long? = null,
    @Json(name = "id_rencana_kerja") val id_rencana_kerja: Long? = null,
    @Json(name = "nomor_spk") val nomor_spk: String? = null,
    @Json(name = "jam_kerja") val jam_kerja: Double? = 7.0,
    @Json(name = "hm_awal") val hm_awal: Double? = null,
    @Json(name = "hm_akhir") val hm_akhir: Double? = null,
    @Json(name = "hm_harian_berjalan") val hm_harian_berjalan: Double? = null,
    @Json(name = "is_isi_solar") val is_isi_solar: Boolean? = false,
    @Json(name = "jumlah_liter_solar") val jumlah_liter_solar: Double? = 0.0,
    @Json(name = "stik_solar_awal") val stik_solar_awal: Double? = null,
    @Json(name = "stik_solar_akhir") val stik_solar_akhir: Double? = null,
    @Json(name = "pemakaian_solar") val pemakaian_solar: Double? = null,
    @Json(name = "kendala_id") val kendala_id: String? = null,
    @Json(name = "kendala_nama") val kendala_nama: String? = null,
    @Json(name = "catatan_kendala") val catatan_kendala: String? = null,
    @Json(name = "daftar_kendala") val daftar_kendala: List<KendalaItemPayload>? = null,
    @Json(name = "hasil_kerja") val hasil_kerja: Double? = 0.0,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "nama_pengawas") val nama_pengawas: String? = null,
    @Json(name = "kode_unit") val kode_unit: String? = null,
    @Json(name = "foto_bukti") val foto_bukti: String? = null,
    // Audit log koreksi HM Awal manual
    @Json(name = "is_hm_awal_corrected") val is_hm_awal_corrected: Boolean? = false,
    @Json(name = "alasan_koreksi_hm") val alasan_koreksi_hm: String? = null,
    @Json(name = "client_transaction_id") val client_transaction_id: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

object KendalaJsonConverter {
    private val moshi by lazy {
        Moshi.Builder().build()
    }
    private val listType by lazy {
        Types.newParameterizedType(List::class.java, KendalaItemPayload::class.java)
    }
    private val adapter by lazy {
        moshi.adapter<List<KendalaItemPayload>>(listType)
    }

    fun toJson(list: List<KendalaItemPayload>): String {
        return try {
            adapter.toJson(list)
        } catch (_: Exception) {
            "[]"
        }
    }

    fun fromJson(json: String?): List<KendalaItemPayload> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

