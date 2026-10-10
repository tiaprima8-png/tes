package com.example.ui.viewmodel

import com.example.data.local.entity.ActivityMasterEntity
import com.example.data.local.entity.KendalaEntity
import com.example.data.local.entity.LokasiEntity
import com.example.data.local.entity.OperatorEntity
import com.example.data.local.entity.RencanaKerjaEntity
import com.example.data.local.entity.UnitEntity
import java.util.UUID

data class UnitPlanStatus(
    val unit: UnitEntity,
    val targetDate: String, // "09/10/2026" or "10/10/2026"
    val isBacklog: Boolean, // true if yesterday
    val labelText: String // "[Tanggal Kemarin: 09/10/2026 - Belum Diisi]" or "[Hari Ini: 10/10/2026]"
)

data class SubRencanaFormState(
    val id: String = UUID.randomUUID().toString(),
    val selectedOperator: OperatorEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val shiftKerja: String = "SIANG",
    val keterangan: String = ""
)

data class RencanaFormUiState(
    val statusUnit: String = "OPERASI", // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val selectedUnitStatus: UnitPlanStatus? = null,
    val selectedUnit: UnitEntity? = null,
    val targetDate: String = "",
    val isBacklog: Boolean = false,
    val selectedOperator: OperatorEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val shiftKerja: String = "SIANG", // "SIANG" | "MALAM"
    val keterangan: String = "",
    val subPlans: List<SubRencanaFormState> = emptyList(),
    val isSubmitting: Boolean = false
)

data class RealisasiFormUiState(
    val selectedRencana: RencanaKerjaEntity? = null,
    val selectedActivity: ActivityMasterEntity? = null,
    val selectedLokasi: LokasiEntity? = null,
    val nomorSpk: String = "",
    val durasiJamKerja: Double = 7.0,
    val hmAwal: Double = 0.0,
    val hmAkhirInput: String = "",
    val hmHarianBerjalan: Double = 0.0,
    val isHmValid: Boolean = true,
    val hmErrorMessage: String? = null,
    val hmMinStandar: Double = 6.0,
    val hmMaxStandar: Double = 10.0,
    val isUnderMinStandard: Boolean = false,
    val selectedKendala: KendalaEntity? = null,
    val catatanKendala: String = "",
    // Parameter Stik Solar BBM
    val stikSolarAwalInput: String = "",
    val stikSolarAkhirInput: String = "",
    val isIsiSolar: Boolean = false,
    val jumlahLiterSolarInput: String = "",
    val pemakaianSolarOtomatis: Double = 0.0,
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
