package com.aspiring_creators.aichopaicho.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy // Added import
import androidx.work.Constraints // Added import
import androidx.work.CoroutineWorker // Added import
import androidx.work.ExistingPeriodicWorkPolicy // Added import
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType // Added import
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder // Added import
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.aspiring_creators.aichopaicho.data.repository.PreferencesRepository
import com.aspiring_creators.aichopaicho.data.repository.SyncRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = userRepository.getUser()

            // Only sync if user is not null, online, and backup is enabled
            if (!user.isOffline && preferencesRepository.isBackupEnabled()) {
                println("BackgroundSyncWorker: Starting upload phase (with server timestamps)...")
                syncRepository.syncContacts()
                syncRepository.syncRecords()
                syncRepository.syncRepayments() // Added sync for the new table
                syncRepository.syncUserData()
                println("BackgroundSyncWorker: Starting upload phase (with server timestamps)...")


                println("BackgroundSyncWorker: Starting download and timestamp-based merge phase...")
                syncRepository.downloadAndMergeData()
                println("BackgroundSyncWorker: Download and merge phase completed.")

                preferencesRepository.setLastSyncTime(System.currentTimeMillis())
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "background_sync"
        const val ONE_TIME_SYNC_WORK_NAME = "background_sync_on_login"


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
                    WorkRequest.MIN_BACKOFF_MILLIS, // Use constant for minimum backoff
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if you want new parameters to take effect
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
            println("BackgroundSyncWorker: One-time sync on login scheduled.")
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            // Optionally cancel the one-time sync too if needed, though it usually runs quickly
            WorkManager.getInstance(context).cancelUniqueWork(ONE_TIME_SYNC_WORK_NAME)
        }
    }
}