package dev.nyxigale.aichopaicho.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.RecurringTemplateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTemplateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTemplateRepository: RecurringTemplateRepository,
    private val recordRepository: RecordRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val dueTemplates = recurringTemplateRepository.getDueTemplates(now)

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
                    description = template.description
                )
                recordRepository.insertRecord(record)
                nextRunAt += intervalMillis
            }

            recurringTemplateRepository.updateNextRun(template.id, nextRunAt)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "recurring_template_worker"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<RecurringTemplateWorker>(
                6,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
