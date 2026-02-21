package dev.nyxigale.aichopaicho.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import dev.nyxigale.aichopaicho.data.repository.PreferencesRepository
import dev.nyxigale.aichopaicho.data.repository.SyncCenterRepository
import dev.nyxigale.aichopaicho.data.repository.SyncRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.data.sync.SyncReport
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    private val syncCenterRepository: SyncCenterRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        var report = SyncReport.EMPTY
        return try {
            val user = userRepository.getUser()

            if (!user.isOffline && preferencesRepository.isBackupEnabled()) {
                val queuedCount = syncRepository.estimateQueueCount()
                syncCenterRepository.beginSync(queuedCount)
                updateProgress(5, "Preparing sync")

                updateProgress(15, "Uploading contacts")
                report += syncRepository.syncContacts()

                updateProgress(35, "Uploading records")
                report += syncRepository.syncRecords()

                updateProgress(55, "Uploading repayments")
                report += syncRepository.syncRepayments()

                updateProgress(70, "Uploading user data")
                report += syncRepository.syncUserData()

                updateProgress(85, "Downloading cloud changes")
                syncRepository.downloadAndMergeData()

                val finishedAt = System.currentTimeMillis()
                preferencesRepository.setLastSyncTime(finishedAt)
                syncCenterRepository.completeSync(report, finishedAt)
                updateProgress(100, "Sync complete")
                Result.success()
            } else {
                syncCenterRepository.markIdle("Sync skipped")
                Result.success()
            }
        } catch (error: Exception) {
            syncCenterRepository.markSyncFailed(report, error.message)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun updateProgress(percent: Int, stage: String) {
        syncCenterRepository.updateInProgress(percent, stage)
        setProgress(
            Data.Builder()
                .putInt(PROGRESS_PERCENT, percent)
                .putString(PROGRESS_STAGE, stage)
                .build()
        )
    }

    companion object {
        const val WORK_NAME = "background_sync"
        const val ONE_TIME_SYNC_WORK_NAME = "background_sync_on_login"
        const val PROGRESS_PERCENT = "progress_percent"
        const val PROGRESS_STAGE = "progress_stage"


        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
                24, TimeUnit.HOURS,
                2, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }


        fun scheduleOneTimeSyncOnLogin(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()

            val oneTimeSyncRequest = OneTimeWorkRequestBuilder<BackgroundSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeSyncRequest
            )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(ONE_TIME_SYNC_WORK_NAME)
        }
    }
}
