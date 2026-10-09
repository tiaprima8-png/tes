package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val unitCode: String, // e.g. "HEH1", "EXC-08", "DT-104"
    val unitType: String,            // e.g. "EXCAVATOR", "DUMP TRUCK", "BULLDOZER", "MOTOR GRADER"
    val brandModel: String,          // e.g. "Komatsu PC200-8", "CAT 320D"
    val lastHm: Double,              // e.g. 3420.5 (used as auto-fill HM Awal)
    val supervisorAssigned: String,  // e.g. "Budi Santoso", "Agus Wijaya"
    val status: String = "OPERASIONAL", // "OPERASIONAL", "STANDBY", "MAINTENANCE"
    val updatedAt: Long = System.currentTimeMillis()
)
