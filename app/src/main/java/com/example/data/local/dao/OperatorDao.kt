package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.OperatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorDao {
    @Query("SELECT * FROM operators ORDER BY name ASC")
    fun getAllOperators(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators WHERE supervisorAssigned = :supervisor ORDER BY name ASC")
    fun getOperatorsForSupervisor(supervisor: String): Flow<List<OperatorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperators(operators: List<OperatorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: OperatorEntity)

    @Update
    suspend fun updateOperator(operator: OperatorEntity)

    @Query("DELETE FROM operators WHERE operatorId = :operatorId")
    suspend fun deleteOperator(operatorId: String)

    @Query("DELETE FROM operators")
    suspend fun clearAllOperators()
}
