package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: Repayment)

    @Query("SELECT * FROM repayments WHERE recordId = :recordId ORDER BY date DESC")
    fun getRepaymentsForRecord(recordId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE id = :repaymentId")
    suspend fun getRepaymentById(repaymentId: String): Repayment?
}
