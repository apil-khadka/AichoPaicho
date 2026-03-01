package dev.nyxigale.aichopaicho.data.repository

import dev.nyxigale.aichopaicho.data.dao.RecordDao
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(private val recordDao: RecordDao) {

    fun getAllRecords(): Flow<List<Record>> {
        return recordDao.getAllRecords()
    }

    suspend fun upsert(record: Record) {
        recordDao.upsert(record)
    }

    suspend fun upsertAll(records: List<Record>) {
        recordDao.upsertAll(records)
    }

     fun getRecordsByContactId(contactId: String): Flow<List<Record>>{
        return recordDao.getRecordsByContactId(contactId)
    }

    // New method to get records with repayments for a specific contact
    fun getRecordsWithRepaymentsByContactId(contactId: String): Flow<List<RecordWithRepayments>> {
        return recordDao.getRecordsWithRepaymentsByContactId(contactId)
    }

    suspend fun getTotalByType(typeId: Int): Int {
        return recordDao.getTotalByType(typeId)
    }

    // New methods needed for the screen
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<RecordWithRepayments>> {
        return recordDao.getRecordsByDateRange(startDate, endDate)
    }

    suspend fun getRecordById(recordId: String): Record? {
        return recordDao.getRecordById(recordId)
    }

    // New method to get a single record with its repayments
    fun getRecordWithRepaymentsById(recordId: String): Flow<RecordWithRepayments?> {
        return recordDao.getRecordWithRepaymentsById(recordId)
    }

    suspend fun updateRecord(record: Record) {
        recordDao.updateRecord(record)
    }

    suspend fun deleteRecord(recordId: String) {
        recordDao.deleteRecord(recordId)
    }

    suspend fun insertRecord(record: Record) {
        recordDao.insertRecord(record)
    }

    suspend fun updateUserId(oldUserId: String, newUserId: String) {
        recordDao.updateUserId(oldUserId, newUserId)
    }

    suspend fun getOpenDueRecordsUntil(endTime: Long): List<Record> {
        return recordDao.getOpenDueRecordsUntil(endTime)
    }
}
