package dev.nyxigale.aichopaicho

import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecurringTemplate
import org.junit.Test
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class RecurringTemplateWorkerPerfTest {

    @Test
    fun benchmarkCurrentVsOptimized() {
        val now = System.currentTimeMillis()
        val oneYearAgo = now - TimeUnit.DAYS.toMillis(365)

        val dueTemplates = (1..50).map { i ->
            RecurringTemplate(
                id = UUID.randomUUID().toString(),
                userId = "user1",
                contactId = "contact1",
                typeId = 1,
                amount = 100,
                description = "Test $i",
                intervalDays = 1,
                nextRunAt = oneYearAgo,
                isActive = true
            )
        }

        var unoptimizedRecordsCreated = 0
        var unoptimizedDbCalls = 0

        val timeUnoptimized = measureTimeMillis {
            for (template in dueTemplates) {
                var nextRunAt = template.nextRunAt
                val intervalMillis = TimeUnit.DAYS.toMillis(template.intervalDays.toLong())
                if (intervalMillis <= 0L) continue

                while (nextRunAt <= now) {
                    val dueDate = if (template.dueOffsetDays > 0) {
                        nextRunAt + TimeUnit.DAYS.toMillis(template.dueOffsetDays.toLong())
                    } else {
                        null
                    }

                    val record = Record(
                        id = UUID.randomUUID().toString(),
                        userId = template.userId,
                        contactId = template.contactId,
                        typeId = template.typeId,
                        amount = template.amount,
                        date = nextRunAt,
                        dueDate = dueDate,
                        description = template.description,
                        recurringTemplateId = template.id
                    )
                    // recordRepository.insertRecord(record)
                    unoptimizedDbCalls++
                    unoptimizedRecordsCreated++
                    nextRunAt += intervalMillis
                }

                // recurringTemplateRepository.updateNextRun(template.id, nextRunAt)
                unoptimizedDbCalls++
            }
        }

        var optimizedRecordsCreated = 0
        var optimizedDbCalls = 0

        val timeOptimized = measureTimeMillis {
            val recordsToInsert = mutableListOf<Record>()

            for (template in dueTemplates) {
                var nextRunAt = template.nextRunAt
                val intervalMillis = TimeUnit.DAYS.toMillis(template.intervalDays.toLong())
                if (intervalMillis <= 0L) continue

                while (nextRunAt <= now) {
                    val dueDate = if (template.dueOffsetDays > 0) {
                        nextRunAt + TimeUnit.DAYS.toMillis(template.dueOffsetDays.toLong())
                    } else {
                        null
                    }

                    val record = Record(
                        id = UUID.randomUUID().toString(),
                        userId = template.userId,
                        contactId = template.contactId,
                        typeId = template.typeId,
                        amount = template.amount,
                        date = nextRunAt,
                        dueDate = dueDate,
                        description = template.description,
                        recurringTemplateId = template.id
                    )
                    recordsToInsert.add(record)
                    optimizedRecordsCreated++
                    nextRunAt += intervalMillis
                }

                // recurringTemplateRepository.updateNextRun(template.id, nextRunAt)
                optimizedDbCalls++
            }

            if (recordsToInsert.isNotEmpty()) {
                // recordRepository.insertRecords(recordsToInsert)
                optimizedDbCalls++
            }
        }

        println("=== Performance Benchmark ===")
        println("Records created: $unoptimizedRecordsCreated")
        println("Unoptimized DB Calls (simulated): $unoptimizedDbCalls")
        println("Optimized DB Calls (simulated): $optimizedDbCalls")
        println("Note: The real performance gain is in reducing I/O operations and transactions.")
        println("DB calls reduced by factor of: ${unoptimizedDbCalls.toFloat() / optimizedDbCalls}")
    }
}
