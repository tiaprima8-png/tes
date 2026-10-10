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

data class KendalaItemFormState(
    val id: String = UUID.randomUUID().toString(),
    val selectedKendala: KendalaEntity? = null,
    val waktuMulai: String = "",
    val waktuSelesai: String = ""
) {
    val durasiMenit: Int?
        get() {
            val startMin = parseTimeToMinutes(waktuMulai) ?: return null
            val endMin = parseTimeToMinutes(waktuSelesai) ?: return null
            if (endMin <= startMin) return null
            return endMin - startMin
        }

    val isTimeRangeValid: Boolean
        get() {
            if (waktuMulai.isBlank() || waktuSelesai.isBlank()) return false
            val startMin = parseTimeToMinutes(waktuMulai) ?: return false
            val endMin = parseTimeToMinutes(waktuSelesai) ?: return false
            return endMin > startMin
        }

    val isValid: Boolean
        get() = selectedKendala != null && isTimeRangeValid

    val durasiLabel: String
        get() {
            val minutes = durasiMenit ?: return ""
            val hours = minutes / 60
            val remainingMins = minutes % 60
            return when {
                hours > 0 && remainingMins > 0 -> "$hours Jam $remainingMins Menit"
                hours > 0 -> "$hours Jam"
                else -> "$remainingMins Menit"
            }
        }

    companion object {
        fun parseTimeToMinutes(timeStr: String): Int? {
            val trimmed = timeStr.trim()
            val parts = trimmed.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return h * 60 + m
        }
    }
}

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
    // Multi-Kendala Form State
    val daftarKendala: List<KendalaItemFormState> = listOf(KendalaItemFormState()),
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
) {
    val totalHambatanMenit: Int
        get() = daftarKendala.mapNotNull { it.durasiMenit }.sum()

    val totalHambatanLabel: String
        get() {
            val total = totalHambatanMenit
            val hours = total / 60
            val minutes = total % 60
            return when {
                total == 0 -> "0 Menit"
                hours > 0 && minutes > 0 -> "$hours Jam $minutes Menit"
                hours > 0 -> "$hours Jam"
                else -> "$minutes Menit"
            }
        }
}

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
