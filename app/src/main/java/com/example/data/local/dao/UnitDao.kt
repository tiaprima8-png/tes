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

    @Query("""
        SELECT * FROM units 
        WHERE (:wilayah IS NOT NULL AND :wilayah != '' AND :wilayah != 'SEMUA' AND :wilayah != 'Semua Wilayah' AND wilayah = :wilayah)
           OR ((:wilayah IS NULL OR :wilayah = '' OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah') AND (supervisorAssigned = :supervisor OR :supervisor = '' OR :supervisor = 'ALL'))
        ORDER BY unitCode ASC
    """)
    fun getUnitsForSupervisorOrWilayah(supervisor: String, wilayah: String?): Flow<List<UnitEntity>>

    @Query("""
        SELECT * FROM units 
        WHERE (:wilayah IS NULL OR :wilayah = 'SEMUA' OR :wilayah = 'Semua Wilayah' OR :wilayah = '' OR wilayah = :wilayah)
          AND (
            :unitType IS NULL OR :unitType = 'Semua Jenis' OR :unitType = '' OR 
            (:unitType LIKE '%EXCAVATOR%' AND (unitType LIKE '%EXCAVATOR%' OR unitType LIKE '%HE%')) OR
            (:unitType LIKE '%DUMP%' AND (unitType LIKE '%DUMP%' OR unitType LIKE '%DT%')) OR
            (:unitType LIKE '%WATER%' AND (unitType LIKE '%WATER%' OR unitType LIKE '%WT%')) OR
            (:unitType LIKE '%BULLDOZER%' AND (unitType LIKE '%BULLDOZER%' OR unitType LIKE '%DOZER%')) OR
            unitType LIKE '%' || :unitType || '%'
          )
          AND (:unitCode IS NULL OR :unitCode = 'Semua Unit' OR :unitCode = '' OR unitCode = :unitCode)
        ORDER BY unitCode ASC
    """)
    fun getUnitsByFilter(wilayah: String?, unitType: String?, unitCode: String?): Flow<List<UnitEntity>>

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
