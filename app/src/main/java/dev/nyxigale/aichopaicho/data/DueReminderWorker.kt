package dev.nyxigale.aichopaicho.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.nyxigale.aichopaicho.MainActivity
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.notification.NotificationChannels
import dev.nyxigale.aichopaicho.data.repository.PreferencesRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DueReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recordRepository: RecordRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!preferencesRepository.isDueReminderEnabled()) return Result.success()
        if (!hasNotificationPermission()) return Result.success()

        val now = System.currentTimeMillis()
        val upcomingWindow = now + TimeUnit.DAYS.toMillis(1)
        val dueRecords = recordRepository.getOpenDueRecordsUntil(upcomingWindow)
        if (dueRecords.isEmpty()) return Result.success()

        val overdueCount = dueRecords.count { (it.dueDate ?: Long.MAX_VALUE) < now }
        val upcomingCount = dueRecords.size - overdueCount
        if (overdueCount == 0 && upcomingCount == 0) return Result.success()

        val contentText = buildString {
            if (overdueCount > 0) append("$overdueCount overdue")
            if (upcomingCount > 0) {
                if (isNotEmpty()) append(" • ")
                append("$upcomingCount due in 24h")
            }
        }

        val openAppIntent = android.content.Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.DUE_REMINDER_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Payment due reminders")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val WORK_NAME = "due_reminder_worker"
        private const val NOTIFICATION_ID = 2001

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<DueReminderWorker>(
                12,
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

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
