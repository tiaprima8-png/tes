package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RencanaKerjaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RencanaKerjaDao {
    @Query("SELECT * FROM rencana_kerja ORDER BY created_at DESC")
    fun getAllRencana(): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja WHERE supervisor_name = :supervisor ORDER BY created_at DESC")
    fun getRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja WHERE supervisor_name = :supervisor AND status_spk != 'SELESAI' ORDER BY created_at DESC")
    fun getActiveRencanaForSupervisor(supervisor: String): Flow<List<RencanaKerjaEntity>>

    @Query("SELECT * FROM rencana_kerja WHERE id = :id LIMIT 1")
    suspend fun getRencanaById(id: Long): RencanaKerjaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRencana(rencana: RencanaKerjaEntity): Long

    @Update
    suspend fun updateRencana(rencana: RencanaKerjaEntity)

    @Query("UPDATE rencana_kerja SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE rencana_kerja SET nomor_spk = :spk, status_spk = :status WHERE id = :id")
    suspend fun updateSpk(id: Long, spk: String, status: String)

    @Query("SELECT * FROM rencana_kerja WHERE is_synced = 0")
    suspend fun getUnsyncedRencanaList(): List<RencanaKerjaEntity>

    @Query("DELETE FROM rencana_kerja WHERE id = :id")
    suspend fun deleteRencana(id: Long)
}
