package com.example

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.model.SupervisorDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun test_hm_delta_calculation() {
        val hmAwal = 4125.5
        val hmAkhir = 4132.5
        val delta = hmAkhir - hmAwal
        assertEquals(7.0, delta, 0.001)
        assertTrue(hmAkhir >= hmAwal)
    }

    @Test
    fun test_hm_invalid_when_less_than_awal() {
        val hmAwal = 4125.5
        val hmAkhir = 4120.0
        assertFalse(hmAkhir >= hmAwal)
    }

    @Test
    fun test_search_multi_criteria_unit() {
        val unit = UnitEntity("HEH1", "EXCAVATOR", "Komatsu PC200-8", 4125.5, "Budi Santoso")
        assertTrue(unit.unitCode.contains("HEH", ignoreCase = true))
        assertTrue(unit.unitType.contains("EXCAVATOR", ignoreCase = true))
        assertTrue(unit.brandModel.contains("Komatsu", ignoreCase = true))
    }

    @Test
    fun test_search_multi_criteria_activity_sap() {
        val act = ActivityMasterEntity("ACT-SAP-101", "Galian Tanah Lunak", "m3")
        assertTrue(act.activityName.contains("gali", ignoreCase = true))
        assertTrue(act.sapCode.contains("101", ignoreCase = true))
    }

    @Test
    fun test_supervisor_authentication_success() {
        val spv1 = SupervisorDirectory.authenticate("SPV-901", "1234")
        assertNotNull(spv1)
        assertEquals("Budi Santoso", spv1?.name)

        val spv2 = SupervisorDirectory.authenticate("Agus Wijaya", "1234")
        assertNotNull(spv2)
        assertEquals("SPV-902", spv2?.nik)
    }

    @Test
    fun test_supervisor_authentication_failure() {
        val invalidSpv = SupervisorDirectory.authenticate("UNKNOWN_SPV", "9999")
        assertNull(invalidSpv)
    }

    @Test
    fun test_image_compression_dimension_landscape() {
        // High-res landscape 4000x3000 (4:3 aspect ratio)
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(4000, 3000, 1280)
        assertEquals(1280, w)
        assertEquals(960, h)
        assertTrue(w <= 1280)
        assertTrue(h <= 1280)
    }

    @Test
    fun test_image_compression_dimension_portrait() {
        // High-res portrait 3000x4000 (3:4 aspect ratio)
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(3000, 4000, 1280)
        assertEquals(960, w)
        assertEquals(1280, h)
        assertTrue(w <= 1280)
        assertTrue(h <= 1280)
    }

    @Test
    fun test_image_compression_dimension_square() {
        // High-res square 2400x2400
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(2400, 2400, 1280)
        assertEquals(1280, w)
        assertEquals(1280, h)
    }

    @Test
    fun test_image_compression_keeps_small_dimension() {
        // Low-res image 800x600 should not be upscaled
        val (w, h) = com.example.util.ImageCompressor.calculateTargetDimensions(800, 600, 1280)
        assertEquals(800, w)
        assertEquals(600, h)
    }

    @Test
    fun test_search_multi_criteria_lokasi() {
        val lok = com.example.data.local.entity.LokasiEntity("001A", "PG1", 10.5, 8.2, "Area Front")
        // Search by kode
        assertTrue(lok.kodeLokasi.contains("001", ignoreCase = true))
        // Search by wilayah
        assertTrue(lok.wilayah.contains("PG1", ignoreCase = true))
    }

    @Test
    fun test_fuel_consumption_ratio_calculation() {
        val totalSolar = 150.0 // Liters
        val totalHm = 10.0 // Hours
        val ratio = totalSolar / totalHm
        assertEquals(15.0, ratio, 0.001)
    }

    @Test
    fun test_operational_work_time_distribution() {
        val operasiHours = 8.0
        val standbyHours = 1.5
        val breakdownHours = 0.5
        val total = operasiHours + standbyHours + breakdownHours
        assertEquals(10.0, total, 0.001)
        val opPercent = operasiHours / total
        assertEquals(0.80, opPercent, 0.001)
    }

    @Test
    fun test_rencana_unrealized_anti_duplication() {
        val rencana1 = com.example.data.local.entity.RencanaKerjaEntity(
            id = 1L,
            supervisorName = "Budi Santoso",
            kodeUnit = "EX-201",
            operator = "Bambang",
            kodeLokasi = "001A",
            isRealized = false,
            statusTahap = "RENCANA"
        )
        val rencana2 = com.example.data.local.entity.RencanaKerjaEntity(
            id = 2L,
            supervisorName = "Budi Santoso",
            kodeUnit = "DZ-101",
            operator = "Hendra",
            kodeLokasi = "PG1",
            isRealized = true,
            statusTahap = "REALISASI_SELESAI"
        )
        val allPlans = listOf(rencana1, rencana2)
        val activePlans = allPlans.filter { !it.isRealized && it.statusTahap == "RENCANA" }
        assertEquals(1, activePlans.size)
        assertEquals("EX-201", activePlans.first().kodeUnit)
    }

    @Test
    fun test_spk_lock_verification() {
        val publishedSpk = "SPK-2026-X101"
        val isLocked = publishedSpk.isNotBlank() && publishedSpk != "Belum Terbit (Menunggu Admin)"
        assertTrue(isLocked)
    }

    @Test
    fun test_kendala_time_duration_calculation() {
        val kendalaItem = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "09:30",
            waktuSelesai = "11:45"
        )
        // 09:30 to 11:45 = 135 minutes (2 Jam 15 Menit)
        assertEquals(135, kendalaItem.durasiMenit)
        assertEquals("2 Jam 15 Menit", kendalaItem.durasiLabel)
        assertTrue(kendalaItem.isTimeRangeValid)
    }

    @Test
    fun test_kendala_invalid_time_range() {
        val invalidEndBeforeStart = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "14:00",
            waktuSelesai = "13:30"
        )
        assertFalse(invalidEndBeforeStart.isTimeRangeValid)
        assertNull(invalidEndBeforeStart.durasiMenit)

        val invalidEqualTimes = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "10:00",
            waktuSelesai = "10:00"
        )
        assertFalse(invalidEqualTimes.isTimeRangeValid)
        assertNull(invalidEqualTimes.durasiMenit)
    }

    @Test
    fun test_kendala_json_serialization_and_deserialization() {
        val payloads = listOf(
            com.example.data.remote.KendalaItemPayload(
                id_kendala = "KND-01",
                nama_kendala = "Hujan",
                waktu_mulai = "09:30",
                waktu_selesai = "11:45",
                durasi_menit = 135
            ),
            com.example.data.remote.KendalaItemPayload(
                id_kendala = "KND-02",
                nama_kendala = "Breakdown Mekanikal",
                waktu_mulai = "13:00",
                waktu_selesai = "14:30",
                durasi_menit = 90
            )
        )

        val json = com.example.data.remote.KendalaJsonConverter.toJson(payloads)
        assertTrue(json.contains("KND-01"))
        assertTrue(json.contains("135"))
        assertTrue(json.contains("Breakdown Mekanikal"))

        val parsed = com.example.data.remote.KendalaJsonConverter.fromJson(json)
        assertEquals(2, parsed.size)
        assertEquals("KND-01", parsed[0].id_kendala)
        assertEquals("09:30", parsed[0].waktu_mulai)
        assertEquals("11:45", parsed[0].waktu_selesai)
        assertEquals(135, parsed[0].durasi_menit)
        assertEquals("Breakdown Mekanikal", parsed[1].nama_kendala)
    }

    @Test
    fun test_official_apk_distribution_urls() {
        val defaultUrl = com.example.ui.viewmodel.HeavyTrackViewModel.APK_DIRECT_DOWNLOAD_URL
        val repoUrl = com.example.ui.viewmodel.HeavyTrackViewModel.GITHUB_REPO_URL

        assertTrue(defaultUrl.startsWith("https://github.com/tiaprima8-png/tes"))
        assertTrue(defaultUrl.endsWith(".apk"))
        assertTrue(repoUrl.contains("tiaprima8-png/tes"))
    }
}
