package dev.nyxigale.aichopaicho.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Upsert
    suspend fun upsert(record: Record)
    
    @Query("SELECT SUM(amount) FROM records WHERE typeId = :typeId AND isDeleted = 0")
    suspend fun getTotalByType(typeId: Int): Int

    @Query("SELECT * FROM records WHERE isDeleted = 0 ORDER BY date ASC")
    fun getAllRecords(): Flow<List<Record>>

    @Transaction
    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<RecordWithRepayments>>

    @Query("SELECT * FROM records WHERE id = :recordId AND isDeleted = 0")
    suspend fun getRecordById(recordId: String): Record?

    @Transaction
    @Query("SELECT * FROM records WHERE id = :recordId AND isDeleted = 0")
    fun getRecordWithRepaymentsById(recordId: String): Flow<RecordWithRepayments?>

    @Transaction
    @Query("SELECT * FROM records WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsWithRepaymentsByContactId(contactId: String): Flow<List<RecordWithRepayments>>

    @Update
    suspend fun updateRecord(record: Record)

    @Query("UPDATE records SET isDeleted = 1, updatedAt = :timestamp WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record)

    @Query("SELECT * FROM records WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsByContactId(contactId: String): Flow<List<Record>>

    @Query("UPDATE records SET userId = :newUserId WHERE userId = :oldUserId")
   suspend  fun updateUserId(oldUserId: String, newUserId: String)

}
