package dev.nyxigale.aichopaicho

import dev.nyxigale.aichopaicho.data.dao.RecordDao
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class MockRecordDao : RecordDao {
    private val records = mutableMapOf<String, Record>()

    override suspend fun upsert(record: Record) {}
    override suspend fun upsertAll(records: List<Record>) {
        records.forEach { this.records[it.id] = it }
    }
    override suspend fun getTotalByType(typeId: Int): Int = 0
    override fun getAllRecords(): Flow<List<Record>> = throw NotImplementedError()
    override fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<RecordWithRepayments>> = throw NotImplementedError()

    override suspend fun getRecordById(recordId: String): Record? {
        return records[recordId]
    }

    override fun getRecordWithRepaymentsById(recordId: String): Flow<RecordWithRepayments?> = throw NotImplementedError()
    override fun getRecordsWithRepaymentsByContactId(contactId: String): Flow<List<RecordWithRepayments>> = throw NotImplementedError()

    override suspend fun updateRecord(record: Record) {
        records[record.id] = record
    }

    override suspend fun deleteRecord(recordId: String, timestamp: Long) {}

    override suspend fun insertRecord(record: Record) {
        records[record.id] = record
    }

    override fun getRecordsByContactId(contactId: String): Flow<List<Record>> = throw NotImplementedError()
    override suspend fun updateUserId(oldUserId: String, newUserId: String) {}
    override suspend fun getOpenDueRecordsUntil(endTime: Long): List<Record> = throw NotImplementedError()

    // Additional methods needed for batching
    override suspend fun getRecordsByIds(recordIds: List<String>): List<Record> {
        return recordIds.mapNotNull { records[it] }
    }

    override suspend fun insertRecords(recordsList: List<Record>) {
        recordsList.forEach { records[it.id] = it }
    }

    override suspend fun updateRecords(recordsList: List<Record>) {
        recordsList.forEach { records[it.id] = it }
    }
}

class PerformanceTest {

    @Test
    fun benchmarkNPlusOneVsBatch() = runBlocking {
        val recordDao = MockRecordDao()
        val numRecords = 500000
        val records = (1..numRecords).map {
            Record(
                id = "record_$it",
                userId = "user_1",
                contactId = "contact_1",
                typeId = 1,
                amount = 100,
                date = System.currentTimeMillis(),
                dueDate = null,
                isComplete = false,
                isDeleted = false,
                description = "Test $it",
                recurringTemplateId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        // Measure N+1 inserts
        val nPlusOneTime = measureTimeMillis {
            for (record in records) {
                recordDao.insertRecord(record)
            }
        }

        println("RESULTS N+1 Insert Time: $nPlusOneTime ms")

        // Now measure batch inserts
        val batchDao = MockRecordDao()
        val batchTime = measureTimeMillis {
            batchDao.insertRecords(records)
        }
        println("RESULTS Batch Insert Time: $batchTime ms")

        // Measure N+1 reads
        val nPlusOneReadTime = measureTimeMillis {
            for (record in records) {
                recordDao.getRecordById(record.id)
            }
        }
        println("RESULTS N+1 Read Time: $nPlusOneReadTime ms")

        // Measure batch reads
        val batchReadTime = measureTimeMillis {
            recordDao.getRecordsByIds(records.map { it.id })
        }
        println("RESULTS Batch Read Time: $batchReadTime ms")

        assert(true)
    }
}
