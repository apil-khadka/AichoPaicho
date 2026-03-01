package dev.nyxigale.aichopaicho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nyxigale.aichopaicho.data.database.AppDatabase
import dev.nyxigale.aichopaicho.data.dao.RecordDao
import dev.nyxigale.aichopaicho.data.entity.Record
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis
import android.util.Log

@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    private lateinit var db: AppDatabase
    private lateinit var recordDao: RecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        recordDao = db.recordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun benchmarkNPlusOneVsBatch() = runBlocking {
        val numRecords = 5000
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
        Log.d("PerformanceTest", "N+1 Insert Time: $nPlusOneTime ms")
        println("RESULTS N+1 Insert Time: $nPlusOneTime ms")

        // Now clear DB and measure batch insert
        db.clearAllTables()
        val batchTime = measureTimeMillis {
            recordDao.insertRecords(records)
        }
        Log.d("PerformanceTest", "Batch Insert Time: $batchTime ms")
        println("RESULTS Batch Insert Time: $batchTime ms")

        // Measure N+1 reads
        val nPlusOneReadTime = measureTimeMillis {
            for (record in records) {
                recordDao.getRecordById(record.id)
            }
        }
        Log.d("PerformanceTest", "N+1 Read Time: $nPlusOneReadTime ms")
        println("RESULTS N+1 Read Time: $nPlusOneReadTime ms")

        // Measure batch reads
        val batchReadTime = measureTimeMillis {
            recordDao.getRecordsByIds(records.map { it.id })
        }
        Log.d("PerformanceTest", "Batch Read Time: $batchReadTime ms")
        println("RESULTS Batch Read Time: $batchReadTime ms")

        // Assert that the test completes
        assert(true)
    }
}
