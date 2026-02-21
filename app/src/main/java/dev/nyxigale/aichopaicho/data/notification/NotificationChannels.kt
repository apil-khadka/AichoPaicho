package dev.nyxigale.aichopaicho.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val DUE_REMINDER_CHANNEL_ID = "due_reminders"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val dueReminderChannel = NotificationChannel(
            DUE_REMINDER_CHANNEL_ID,
            "Due Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for overdue and upcoming dues"
        }
        manager.createNotificationChannel(dueReminderChannel)
    }
}
