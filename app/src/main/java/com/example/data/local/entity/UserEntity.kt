package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users_session")
data class UserEntity(
    @PrimaryKey
    val username: String,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "nik")
    val nik: String = "",

    @ColumnInfo(name = "role")
    @SerializedName("role")
    val role: String = "SUPERVISOR", // "SUPERVISOR" vs "MANAGER"

    @ColumnInfo(name = "role_title")
    val roleTitle: String = "Supervisor",

    @ColumnInfo(name = "default_shift")
    val defaultShift: String = "SIANG",

    @ColumnInfo(name = "assigned_sector")
    val assignedSector: String = "Sektor Operasional Lapangan",

    @ColumnInfo(name = "phone")
    val phone: String = "",

    @ColumnInfo(name = "is_active_session")
    val isActiveSession: Boolean = true,

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long = System.currentTimeMillis()
)
