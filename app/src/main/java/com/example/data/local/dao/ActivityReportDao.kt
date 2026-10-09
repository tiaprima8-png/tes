package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityReportDao {
    @Query("SELECT * FROM activity_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ActivityReportEntity>>

    @Query("SELECT * FROM activity_reports WHERE supervisorName = :supervisor ORDER BY createdAt DESC")
    fun getReportsForSupervisor(supervisor: String): Flow<List<ActivityReportEntity>>

    @Query("SELECT * FROM activity_reports WHERE isSynced = 0 ORDER BY createdAt ASC")
    fun getUnsyncedReports(): Flow<List<ActivityReportEntity>>

    @Query("SELECT * FROM activity_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReportsList(): List<ActivityReportEntity>

    @Query("SELECT COUNT(*) FROM activity_reports WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ActivityReportEntity): Long

    @Update
    suspend fun updateReport(report: ActivityReportEntity)

    @Query("UPDATE activity_reports SET isSynced = 1, syncedAt = :timestamp WHERE id = :reportId")
    suspend fun markReportAsSynced(reportId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM activity_reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: Long)

    @Query("DELETE FROM activity_reports")
    suspend fun clearAllReports()
}
