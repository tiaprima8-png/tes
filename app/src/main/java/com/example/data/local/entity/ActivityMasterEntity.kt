package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_activities")
data class ActivityMasterEntity(
    @PrimaryKey val sapCode: String,      // e.g. "ACT-SAP-101", "ACT-SAP-102"
    val activityName: String,             // e.g. "Galian Tanah Lunak", "Loading Overburden"
    val unitOfMeasure: String,            // e.g. "m3", "m2", "m", "ton", "rit"
    val description: String = "",
    val category: String = "PRODUKSI",    // "PRODUKSI", "SUPPORT", "HAULING", "INFRASTRUKTUR"
    val updatedAt: Long = System.currentTimeMillis()
)
