package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "activity_reports")
data class ActivityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientUuid: String = UUID.randomUUID().toString(),
    val reportDate: String,              // Format: "DD/MM/YYYY"
    val supervisorName: String,          // Active supervisor
    val unitCode: String,                // Selected Unit Code e.g. "HEH1"
    val unitType: String,                // e.g. "EXCAVATOR"
    val activityName: String,            // e.g. "Galian Tanah Lunak"
    val sapCode: String,                 // e.g. "ACT-SAP-101"
    val unitOfMeasure: String,           // e.g. "m3"
    val operatorName: String,            // Selected Operator e.g. "Ahmad Fauzi"
    val workLocation: String,            // e.g. "001A / Mainroad"
    val shift: String,                   // "SIANG" or "MALAM"
    val workDurationHours: Double = 7.0, // Default 7.00 hours
    val hmAwal: Double,                  // Read-only from last HM
    val hmAkhir: Double,                 // Input by supervisor
    val hmDailyRunning: Double,          // (hmAkhir - hmAwal)
    val workOutput: Double,              // Numeric result
    val notes: String = "",              // Field notes
    val unitStatus: String = "OPERASI",  // "OPERASI" | "STANDBY" | "BREAKDOWN"
    val isRefueled: Boolean = false,     // Fuel refuel toggle
    val fuelLiters: Double = 0.0,        // Liter solar
    val photoPath: String? = null,       // Local compressed JPEG path
    val photoBase64: String? = null,     // Base64 data string (for sync)
    val photoSizeKb: Long? = null,       // Size in KB (e.g. 145)
    val photoDimensions: String? = null, // e.g. "1280x960 px"
    val isSynced: Boolean = false,       // 0 = pending/offline, 1 = synced
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
