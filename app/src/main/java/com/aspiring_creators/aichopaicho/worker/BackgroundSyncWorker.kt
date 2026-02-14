package com.aspiring_creators.aichopaicho.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.data.repository.RepaymentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val loanRepository: LoanRepository,
    private val repaymentRepository: RepaymentRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background sync...")
        try {
            // Simulate sync logic
            // In a real app, this would:
            // 1. Fetch un-synced loans/repayments from local DB (need 'synced' flag)
            // 2. Push to backend
            // 3. Update 'synced' flag
            // 4. Fetch changes from backend

            // For now, just log and delay
            delay(2000)
            Log.d(TAG, "Sync completed successfully.")

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "BackgroundSyncWorker"
        private const val SYNC_WORK_NAME = "sync_work"

        fun schedulePeriodicSync(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun scheduleOneTimeSyncOnLogin(context: Context) {
             val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<BackgroundSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}
