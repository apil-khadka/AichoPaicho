package com.aspiring_creators.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.LoanWithContact
import com.aspiring_creators.aichopaicho.data.entity.LoanWithRepayments
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: Loan)

    @Update
    suspend fun update(loan: Loan)

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: String): Loan?

    @Query("SELECT * FROM loans WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getLoansByContact(contactId: String): Flow<List<Loan>>

    @Transaction
    @Query("SELECT * FROM loans WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getLoansWithRepayments(contactId: String): Flow<List<LoanWithRepayments>>

    @Query("SELECT * FROM loans WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Transaction
    @Query("SELECT * FROM loans WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecentLoansWithContact(limit: Int): Flow<List<LoanWithContact>>

    @Query("UPDATE loans SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun delete(id: String, timestamp: Long = System.currentTimeMillis())
}
