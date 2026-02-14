package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repayment: Repayment)

    @Update
    suspend fun update(repayment: Repayment)

    @Query("SELECT * FROM repayments WHERE id = :id")
    suspend fun getRepaymentById(id: String): Repayment?

    @Query("SELECT * FROM repayments WHERE loanId = :loanId AND isDeleted = 0 ORDER BY date DESC")
    fun getRepaymentsByLoan(loanId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE isDeleted = 0")
    fun getAllRepayments(): Flow<List<Repayment>>

    @Query("UPDATE repayments SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun delete(id: String, timestamp: Long = System.currentTimeMillis())
}
