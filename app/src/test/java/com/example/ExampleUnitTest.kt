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
}
