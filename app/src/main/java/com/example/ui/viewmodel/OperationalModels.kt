package com.example.ui.viewmodel

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RencanaFormUiState(
    val statusUnit: String = "OPERASI", // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val selectedUnit: UnitEntity? = null,
    val selectedOperator: OperatorEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val shiftKerja: String = "SIANG", // "SIANG" | "MALAM"
    val keterangan: String = "",
    val isSubmitting: Boolean = false
)

data class RealisasiFormUiState(
    val selectedRencana: RencanaKerjaEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val nomorSpk: String = "",
    val durasiJamKerja: Double = 7.0,
    val hmAwal: Double = 0.0,
    val hmAkhirInput: String = "",
    val hmHarianBerjalan: Double = 0.0,
    val isHmValid: Boolean = true,
    val hmErrorMessage: String? = null,
    val isIsiSolar: Boolean = false,
    val jumlahLiterSolarInput: String = "",
    val hasilKerjaInput: String = "",
    val keterangan: String = "",
    val photoPath: String? = null,
    val photoBase64: String? = null,
    val photoSizeKb: Long? = null,
    val photoDimensions: String? = null,
    val isCompressingPhoto: Boolean = false,
    val isSubmitting: Boolean = false
)

data class UnitAnalyticsRow(
    val unitCode: String,
    val model: String,
    val totalWorkHours: Double,
    val totalHmRunning: Double,
    val totalFuelLiters: Double,
    val efficiencyRatio: Double, // L/HM
    val standbyBreakdownCount: Int,
    val totalOutputVolume: Double,
    val unitOfMeasure: String
)

data class ActivityAnalyticsRow(
    val activityName: String,
    val sapCode: String,
    val unitOfMeasure: String,
    val totalOutputVolume: Double,
    val totalHm: Double,
    val unitInvolvedCount: Int
)

data class FleetAnalyticsState(
    val selectedPeriod: String = "Bulan Ini", // "Bulan Ini", "30 Hari", "Semua"
    val selectedUnitFilter: String = "Semua Unit",
    val totalSolarLiters: Double = 0.0,
    val totalHmRunning: Double = 0.0,
    val avgFuelConsumptionRatio: Double = 0.0, // Liter / HM
    val effectiveOperationHours: Double = 0.0,
    val standbyHours: Double = 0.0,
    val breakdownHours: Double = 0.0,
    val operationPercent: Float = 0f,
    val standbyPercent: Float = 0f,
    val breakdownPercent: Float = 0f,
    val unitSummaries: List<UnitAnalyticsRow> = emptyList(),
    val activitySummaries: List<ActivityAnalyticsRow> = emptyList()
)
