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
    val hmMinStandar: Double = 6.0,  // Standar minimal HM (misal 6.0 HM)
    val hmMaxStandar: Double = 10.0, // Standar maksimal HM (misal 10.0 HM)
    val wilayah: String = "PG1",     // e.g. "PG1", "PG2", "PG3"
    val updatedAt: Long = System.currentTimeMillis()
)
