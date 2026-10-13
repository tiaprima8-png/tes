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
    val role: String = "KASIE", // "KASIE" (alias "SUPERVISOR") vs "MANAGER"

    @ColumnInfo(name = "role_title")
    val roleTitle: String = "Kasie",

    @ColumnInfo(name = "wilayah")
    val wilayah: String = "PG1", // "PG1", "PG2", "PG3", or "Semua Wilayah" for Manager

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
) {
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isSupervisor: Boolean get() = !isManager
    val isKasie: Boolean get() = !isManager
}
