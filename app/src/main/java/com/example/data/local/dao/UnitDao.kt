package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY unitCode ASC")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE supervisorAssigned = :supervisor ORDER BY unitCode ASC")
    fun getUnitsForSupervisor(supervisor: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE unitCode = :unitCode LIMIT 1")
    suspend fun getUnitByCode(unitCode: String): UnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity)

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Query("UPDATE units SET lastHm = :newLastHm, updatedAt = :timestamp WHERE unitCode = :unitCode")
    suspend fun updateUnitHm(unitCode: String, newLastHm: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM units WHERE unitCode = :unitCode")
    suspend fun deleteUnit(unitCode: String)

    @Query("DELETE FROM units")
    suspend fun clearAllUnits()
}
