package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RKCE", appName)
  }

  @Test
  fun `verify SPK_PUBLISHED json payload parsing and status`() {
    val jsonString = """{"unit_kode": "DT-102", "nomor_spk": "SPK/CE/2026/10/375", "id_rencana_kerja": 102}"""
    val json = org.json.JSONObject(jsonString)
    val unitKode = json.getString("unit_kode")
    val nomorSpk = json.getString("nomor_spk")
    val rencanaId = json.optInt("id_rencana_kerja")

    assertEquals("DT-102", unitKode)
    assertEquals("SPK/CE/2026/10/375", nomorSpk)
    assertEquals(102, rencanaId)

    val waitingPlan = com.example.data.local.entity.RencanaKerjaEntity(
        id = rencanaId.toLong(),
        supervisorName = "Budi Santoso",
        kodeUnit = unitKode,
        operator = "Ahmad Fauzi",
        kodeLokasi = "001A",
        nomorSpk = "",
        statusSpk = "MENUNGGU"
    )
    org.junit.Assert.assertFalse(waitingPlan.isSpkTerbit)

    val publishedPlan = waitingPlan.copy(
        nomorSpk = nomorSpk,
        statusSpk = "SPK_TERBIT"
    )
    org.junit.Assert.assertTrue(publishedPlan.isSpkTerbit)
    assertEquals("SPK/CE/2026/10/375", publishedPlan.nomorSpk)
  }
}
