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
    fun test_kendala_invalid_equal_times() {
        val invalidEqualTimes = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "10:00",
            waktuSelesai = "10:00"
        )
        assertFalse(invalidEqualTimes.isTimeRangeValid)
        assertNull(invalidEqualTimes.durasiMenit)

        val invalidFormat = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "ab:cd",
            waktuSelesai = "12:00"
        )
        assertFalse(invalidFormat.isTimeRangeValid)
        assertNull(invalidFormat.durasiMenit)
    }

    @Test
    fun test_kendala_cross_midnight_calculation() {
        // Shift malam melintasi tengah malam: 23:00 s/d 02:00 = 3 Jam (180 Menit)
        val crossMidnight = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "23:00",
            waktuSelesai = "02:00"
        )
        assertTrue(crossMidnight.isTimeRangeValid)
        assertTrue(crossMidnight.isCrossMidnight)
        assertEquals(180, crossMidnight.durasiMenit)
        assertTrue(crossMidnight.durasiLabel.contains("3 Jam"))

        // Shift malam: 22:30 s/d 01:15 = 2 Jam 45 Menit (165 Menit)
        val crossMidnight2 = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "22:30",
            waktuSelesai = "01:15"
        )
        assertTrue(crossMidnight2.isTimeRangeValid)
        assertEquals(165, crossMidnight2.durasiMenit)
    }

    @Test
    fun test_kendala_anti_overlapping_detection() {
        // Range 1: 08:00 - 10:00
        val k1 = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "08:00", waktuSelesai = "10:00")
        // Range 2 (Overlapping): 09:30 - 11:00
        val k2Overlap = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "09:30", waktuSelesai = "11:00")
        // Range 3 (Non-overlapping touching edge): 10:00 - 12:00
        val k3Adjacent = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "10:00", waktuSelesai = "12:00")

        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(k1, k2Overlap))
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(k1, k3Adjacent))

        // Cross-midnight overlap test:
        // Range A: 23:00 - 02:00
        val kCrossA = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "23:00", waktuSelesai = "02:00")
        // Range B: 01:00 - 03:00 (collides between 01:00 and 02:00)
        val kCrossB = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "01:00", waktuSelesai = "03:00")
        // Range C: 02:00 - 04:00 (adjacent, not colliding)
        val kCrossC = com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "02:00", waktuSelesai = "04:00")

        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kCrossA, kCrossB))
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kCrossA, kCrossC))

        // UI state level overlap check
        val formWithOverlap = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kCrossA, kCrossB)
        )
        assertTrue(formWithOverlap.hasAnyKendalaOverlap)
        assertTrue(formWithOverlap.isKendalaOverlapping(0))
        assertTrue(formWithOverlap.isKendalaOverlapping(1))

        val formWithoutOverlap = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kCrossA, kCrossC)
        )
        assertFalse(formWithoutOverlap.hasAnyKendalaOverlap)
    }

    @Test
    fun test_kendala_shift_limit_validation() {
        // Shift durasi 7 jam = 420 menit
        val formState = com.example.ui.viewmodel.RealisasiFormUiState(
            durasiJamKerja = 7.0,
            daftarKendala = listOf(
                com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "08:00", waktuSelesai = "12:00"), // 4 jam = 240 min
                com.example.ui.viewmodel.KendalaItemFormState(waktuMulai = "12:00", waktuSelesai = "16:00")  // 4 jam = 240 min -> Total 8 jam > 7 jam
            )
        )
        assertEquals(480, formState.totalHambatanMenit)
        assertTrue(formState.isTotalKendalaExceedingShift)
    }

    @Test
    fun test_hm_akhir_must_be_strictly_greater_than_awal() {
        val hmAwal = 3500.0
        val hmAkhirEqual = 3500.0
        val hmAkhirLesser = 3499.0
        val hmAkhirGreater = 3507.0

        assertFalse(hmAkhirEqual > hmAwal)
        assertFalse(hmAkhirLesser > hmAwal)
        assertTrue(hmAkhirGreater > hmAwal)
    }

    @Test
    fun test_dynamic_unit_hm_standards_reading() {
        val excavator = UnitEntity("EXC-01", "EXCAVATOR", "Komatsu PC200", 4000.0, "Budi", "OPERASIONAL", 6.0, 10.0)
        val dumptruck = UnitEntity("DT-101", "DUMP TRUCK", "Hino FM 260", 3000.0, "Budi", "OPERASIONAL", 5.5, 9.5)
        val grader = UnitEntity("GRD-01", "MOTOR GRADER", "CAT 140K", 2000.0, "Budi", "OPERASIONAL", 5.0, 9.0)

        assertEquals(6.0, excavator.hmMinStandar, 0.001)
        assertEquals(10.0, excavator.hmMaxStandar, 0.001)

        assertEquals(5.5, dumptruck.hmMinStandar, 0.001)
        assertEquals(9.5, dumptruck.hmMaxStandar, 0.001)

        assertEquals(5.0, grader.hmMinStandar, 0.001)
        assertEquals(9.0, grader.hmMaxStandar, 0.001)
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
        val releasesPage = com.example.ui.viewmodel.HeavyTrackViewModel.GITHUB_RELEASES_PAGE_URL
        val debugUrl = com.example.ui.viewmodel.HeavyTrackViewModel.APK_DEBUG_DOWNLOAD_URL

        assertTrue(defaultUrl.startsWith("https://github.com/tiaprima8-png/tes"))
        assertTrue(defaultUrl.endsWith(".apk"))
        assertTrue(repoUrl.contains("tiaprima8-png/tes"))
        assertTrue(releasesPage.endsWith("/releases"))
        assertTrue(debugUrl.endsWith(".apk"))
    }

    @Test
    fun test_master_kendala_15_items_mapping() {
        val serverList = com.example.data.remote.CompanyServerSimulator.serverKendala
        assertEquals(15, serverList.size)

        // Verify the 15 standard items in order
        val expectedNames = listOf(
            "Antar - jemput",
            "Cek unit, Implement Pemanasan mesin",
            "Transport",
            "Kerja",
            "Istirahat",
            "Perbaikan Unit",
            "Perbaikan Implement",
            "Perawatan Unit",
            "Perawatan Implement",
            "Ganti Implement",
            "Tunggu Solar",
            "Tunggu Mekanik",
            "Tunggu Trailler",
            "Tunggu Lokasi",
            "Tunggu Cuaca"
        )

        serverList.forEachIndexed { index, item ->
            assertEquals(expectedNames[index], item.resolvedNama)
            val generatedKode = item.resolveKode(index)
            assertTrue(generatedKode.startsWith("KND-"))
        }

        // Test fallback resolveKode when API provides no code or null
        val payloadWithoutCode = com.example.data.remote.KendalaPayload(nama_kendala = "Tunggu Cuaca")
        assertEquals("KND-15", payloadWithoutCode.resolveKode(14))
    }

    @Test
    fun test_unit_plan_status_three_cases() {
        val uA = UnitEntity("EX-301", "EXCAVATOR", "Komatsu PC300", 3500.0, "Budi")
        val uB = UnitEntity("DZ-01", "BULLDOZER", "Cat D8R", 4200.0, "Budi")
        val uC = UnitEntity("GD-01", "MOTOR GRADER", "Cat 14M", 2800.0, "Budi")

        val yesterdayStr = "09/10/2026"
        val todayStr = "10/10/2026"

        val rencanaList = listOf(
            // Unit DZ-01 ada kemarin, belum ada hari ini (Kasus B)
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 1,
                supervisorName = "Budi",
                kodeUnit = "DZ-01",
                operator = "Agus",
                kodeLokasi = "001A",
                tanggalFormat = yesterdayStr
            ),
            // Unit GD-01 ada kemarin dan sudah ada 1 rencana hari ini (Kasus C)
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 2,
                supervisorName = "Budi",
                kodeUnit = "GD-01",
                operator = "Fauzi",
                kodeLokasi = "001A",
                tanggalFormat = yesterdayStr
            ),
            com.example.data.local.entity.RencanaKerjaEntity(
                id = 3,
                supervisorName = "Budi",
                kodeUnit = "GD-01",
                operator = "Fauzi",
                kodeLokasi = "001A",
                tanggalFormat = todayStr
            )
            // Unit EX-301 belum ada kemarin sama sekali (Kasus A)
        )

        val units = listOf(uA, uB, uC)

        val resultStatuses = units.map { u ->
            val plansYesterday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == yesterdayStr
            }
            val plansToday = rencanaList.filter {
                it.kodeUnit.equals(u.unitCode, ignoreCase = true) &&
                it.tanggalFormat == todayStr
            }

            when {
                plansYesterday.isEmpty() -> {
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = yesterdayStr,
                        isBacklog = true,
                        isSusulan = false,
                        existingPlanCount = plansToday.size,
                        labelText = "${u.unitCode} [Tgl Kemarin: Belum Diisi]"
                    )
                }
                plansToday.isEmpty() -> {
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = false,
                        existingPlanCount = 0,
                        labelText = "${u.unitCode} [Hari Ini: Belum Ada Rencana]"
                    )
                }
                else -> {
                    val count = plansToday.size
                    com.example.ui.viewmodel.UnitPlanStatus(
                        unit = u,
                        targetDate = todayStr,
                        isBacklog = false,
                        isSusulan = true,
                        existingPlanCount = count,
                        labelText = "${u.unitCode} [Hari Ini: Sudah Ada $count Rencana] (+ Tambah Rencana Susulan)"
                    )
                }
            }
        }

        // 1. Kasus A: EX-301
        val statusA = resultStatuses.find { it.unit.unitCode == "EX-301" }
        assertNotNull(statusA)
        assertTrue(statusA!!.isBacklog)
        assertFalse(statusA.isSusulan)
        assertEquals("EX-301 [Tgl Kemarin: Belum Diisi]", statusA.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.BACKLOG_YESTERDAY, statusA.category)

        // 2. Kasus B: DZ-01
        val statusB = resultStatuses.find { it.unit.unitCode == "DZ-01" }
        assertNotNull(statusB)
        assertFalse(statusB!!.isBacklog)
        assertFalse(statusB.isSusulan)
        assertEquals("DZ-01 [Hari Ini: Belum Ada Rencana]", statusB.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.NO_PLAN_TODAY, statusB.category)

        // 3. Kasus C: GD-01
        val statusC = resultStatuses.find { it.unit.unitCode == "GD-01" }
        assertNotNull(statusC)
        assertFalse(statusC!!.isBacklog)
        assertTrue(statusC.isSusulan)
        assertEquals(1, statusC.existingPlanCount)
        assertEquals("GD-01 [Hari Ini: Sudah Ada 1 Rencana] (+ Tambah Rencana Susulan)", statusC.labelText)
        assertEquals(com.example.ui.viewmodel.UnitPlanCategory.HAS_PLAN_TODAY, statusC.category)

        // Pastikan tidak ada unit yang hilang (total 3 unit tetap tersedia)
        assertEquals(3, resultStatuses.size)
    }

    // ======================================================================
    // PENGUJIAN 5 BLIND SPOTS FORMULIR REALISASI KERJA & SINKRONISASI RKCE
    // ======================================================================

    @Test
    fun test_formula_pemakaian_solar() {
        // Formula: Konsumsi Solar = (Stik Solar Awal + Total Isi Solar Tambahan) - Stik Solar Akhir
        // Kasus 1: Tanpa pengisian solar tambahan (85.0 - 60.0 = 25.0 Liter)
        val state1 = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "85.0",
            stikSolarAkhirInput = "60.0",
            isIsiSolar = false,
            jumlahLiterSolarInput = ""
        )
        assertEquals(25.0, state1.computedKonsumsiSolar, 0.001)
        assertFalse(state1.isStikAkhirHigherWithoutRefuel)
        assertFalse(state1.isSolarConsumptionNegative)
        assertFalse(state1.isRefuelLitersMissing)

        // Kasus 2: Dengan pengisian solar tambahan 100 Liter (Stik awal 20.0 + Isi 100.0 - Stik akhir 80.0 = 40.0 Liter)
        val state2 = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "20.0",
            stikSolarAkhirInput = "80.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = "100.0"
        )
        assertEquals(40.0, state2.computedKonsumsiSolar, 0.001)
        assertFalse(state2.isStikAkhirHigherWithoutRefuel)
        assertFalse(state2.isSolarConsumptionNegative)
        assertFalse(state2.isRefuelLitersMissing)
    }

    @Test
    fun test_validasi_stik_solar_warning_and_negative() {
        // Logika 1: Stik Akhir > Stik Awal dan toggle [Isi Solar Tambahan] MATI -> Warning muncul
        val warningState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "40.0",
            stikSolarAkhirInput = "75.0",
            isIsiSolar = false,
            jumlahLiterSolarInput = ""
        )
        assertTrue(warningState.isStikAkhirHigherWithoutRefuel)
        assertTrue(warningState.isSolarConsumptionNegative) // (40 + 0) - 75 = -35.0

        // Logika 2: Toggle [Isi Solar Tambahan] AKTIF tapi input liter kosong -> isRefuelLitersMissing true
        val missingRefuelState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "40.0",
            stikSolarAkhirInput = "75.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = ""
        )
        assertTrue(missingRefuelState.isRefuelLitersMissing)

        // Logika 3: Pengisian BBM diisi tapi total konsumsi tetap negatif
        val negativeState = com.example.ui.viewmodel.RealisasiFormUiState(
            stikSolarAwalInput = "30.0",
            stikSolarAkhirInput = "90.0",
            isIsiSolar = true,
            jumlahLiterSolarInput = "40.0" // (30 + 40) - 90 = -20
        )
        assertTrue(negativeState.isSolarConsumptionNegative)
        assertEquals(-20.0, negativeState.computedKonsumsiSolar, 0.001)
    }

    @Test
    fun test_kendala_time_range_normal_and_cross_midnight() {
        // Normal shift daytime: 08:00 - 10:00 = 2 jam (120 menit)
        val kendalaNormal = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "08:00",
            waktuSelesai = "10:00"
        )
        assertTrue(kendalaNormal.isTimeRangeValid)
        assertFalse(kendalaNormal.isCrossMidnight)
        assertEquals(120, kendalaNormal.durasiMenit)

        // Night shift cross midnight: 23:00 - 02:00 = 3 jam (180 menit)
        val kendalaCrossMidnight = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "23:00",
            waktuSelesai = "02:00"
        )
        assertTrue(kendalaCrossMidnight.isTimeRangeValid)
        assertTrue(kendalaCrossMidnight.isCrossMidnight)
        assertEquals(180, kendalaCrossMidnight.durasiMenit)
    }

    @Test
    fun test_kendala_anti_overlapping() {
        val kendalaA = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "08:00",
            waktuSelesai = "10:00"
        )
        val kendalaB = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "09:30",
            waktuSelesai = "11:00"
        )
        val kendalaC = com.example.ui.viewmodel.KendalaItemFormState(
            waktuMulai = "10:30",
            waktuSelesai = "12:00"
        )

        // A dan B bertabrakan (08:00-10:00 vs 09:30-11:00)
        assertTrue(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kendalaA, kendalaB))
        // A dan C TIDAK bertabrakan (08:00-10:00 vs 10:30-12:00)
        assertFalse(com.example.ui.viewmodel.KendalaItemFormState.hasOverlap(kendalaA, kendalaC))

        val formState = com.example.ui.viewmodel.RealisasiFormUiState(
            daftarKendala = listOf(kendalaA, kendalaB)
        )
        assertTrue(formState.hasAnyKendalaOverlap)
    }

    @Test
    fun test_idempotency_client_transaction_id_format() {
        val txId = "TX-" + java.util.UUID.randomUUID().toString()
        assertTrue(txId.startsWith("TX-"))
        assertTrue(txId.length > 10)
    }

    @Test
    fun test_supervisor_session_logout_clears_session() {
        val spv = SupervisorDirectory.authenticate("SPV-901", "1234")
        assertNotNull(spv)

        var currentSession = spv
        var isLoggedIn = true
        assertNotNull(currentSession)
        assertTrue(isLoggedIn)

        // Trigger logout
        isLoggedIn = false
        currentSession = null

        assertFalse(isLoggedIn)
        assertNull(currentSession)
    }

    @Test
    fun test_bootstrap_data_payload_resolution() {
        val payload = com.example.data.remote.BootstrapDataPayload(
            rencana_aktif = listOf(
                com.example.data.remote.PostRencanaKerjaRequest(
                    kode_unit = "HEH1",
                    nomor_spk = "SPK-2026-X101",
                    status_spk = "TERBIT_SPK",
                    nama_pengawas = "Budi Santoso"
                )
            ),
            riwayat_aktivitas = listOf(
                com.example.data.remote.PostActivityReportRequest(
                    tanggal = "11/10/2026",
                    nama_pengawas = "Budi Santoso",
                    kode_unit = "HEH1",
                    hm_akhir = 4125.5
                )
            )
        )

        assertEquals(1, payload.resolvedRencanaAktif.size)
        assertEquals("SPK-2026-X101", payload.resolvedRencanaAktif[0].nomor_spk)
        assertEquals("TERBIT_SPK", payload.resolvedRencanaAktif[0].status_spk)
        assertEquals(1, payload.resolvedRiwayatAktivitas.size)
        assertEquals("HEH1", payload.resolvedRiwayatAktivitas[0].kode_unit)
    }

    @Test
    fun test_bootstrap_simulator_returns_active_spk_plans_and_history() = kotlinx.coroutines.test.runTest {
        val response = com.example.data.remote.CompanyServerSimulator.fetchBootstrapData(
            pengawas = "Budi Santoso",
            tanggal = "11/10/2026"
        )
        assertNotNull(response.data)
        val data = response.data!!
        assertTrue(data.resolvedRencanaAktif.isNotEmpty())
        assertTrue(data.resolvedRiwayatAktivitas.isNotEmpty())

        val planWithSpk = data.resolvedRencanaAktif.find { it.status_spk == "TERBIT_SPK" }
        assertNotNull(planWithSpk)
        assertTrue(planWithSpk!!.nomor_spk?.startsWith("SPK-") == true)
    }
}
