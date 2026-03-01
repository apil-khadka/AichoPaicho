package dev.nyxigale.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.nyxigale.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: Repayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayments(repayments: List<Repayment>)

    @Query("SELECT * FROM repayments WHERE recordId = :recordId ORDER BY date DESC")
    fun getRepaymentsForRecord(recordId: String): Flow<List<Repayment>>

    @Query("SELECT * FROM repayments WHERE id = :repaymentId")
    suspend fun getRepaymentById(repaymentId: String): Repayment?

    @Query("SELECT * FROM repayments ORDER BY date DESC")
    suspend fun getAllRepayments(): List<Repayment>
}
