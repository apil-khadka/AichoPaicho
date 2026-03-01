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

    @Upsert
    suspend fun upsertAll(records: List<Record>)

    @Query("SELECT SUM(amount) FROM records WHERE typeId = :typeId AND isDeleted = 0")
    suspend fun getTotalByType(typeId: Int): Int

    @Query("SELECT * FROM records WHERE isDeleted = 0 ORDER BY date ASC")
    fun getAllRecords(): Flow<List<Record>>

    @Transaction
    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<RecordWithRepayments>>

    @Query("SELECT * FROM records WHERE id = :recordId AND isDeleted = 0")
    suspend fun getRecordById(recordId: String): Record?

    @Query("SELECT * FROM records WHERE id IN (:recordIds)")
    suspend fun getRecordsByIds(recordIds: List<String>): List<Record>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :recordId AND isDeleted = 0")
    fun getRecordWithRepaymentsById(recordId: String): Flow<RecordWithRepayments?>

    @Transaction
    @Query("SELECT * FROM records WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsWithRepaymentsByContactId(contactId: String): Flow<List<RecordWithRepayments>>

    @Update
    suspend fun updateRecord(record: Record)

    @Update
    suspend fun updateRecords(records: List<Record>)

    @Query("UPDATE records SET isDeleted = 1, updatedAt = :timestamp WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<Record>)

    @Query("SELECT * FROM records WHERE contactId = :contactId AND isDeleted = 0 ORDER BY date DESC")
    fun getRecordsByContactId(contactId: String): Flow<List<Record>>

    @Query("UPDATE records SET userId = :newUserId WHERE userId = :oldUserId")
   suspend  fun updateUserId(oldUserId: String, newUserId: String)

    @Query(
        "SELECT * FROM records WHERE dueDate IS NOT NULL AND dueDate <= :endTime AND isDeleted = 0 AND isComplete = 0 ORDER BY dueDate ASC"
    )
    suspend fun getOpenDueRecordsUntil(endTime: Long): List<Record>

}
