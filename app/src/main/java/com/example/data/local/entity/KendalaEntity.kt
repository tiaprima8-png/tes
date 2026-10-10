package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_kendala")
data class KendalaEntity(
    @PrimaryKey val kodeKendala: String, // e.g. "KND-01"
    val namaKendala: String,            // e.g. "Hujan / Slippery (Jalan Licin Tambang)"
    val kategori: String = "OPERASIONAL", // "CUACA", "MEKANIKAL", "OPERASIONAL", "SAFETY"
    val deskripsi: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
