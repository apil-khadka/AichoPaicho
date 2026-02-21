package dev.nyxigale.aichopaicho.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun getCurrency(): String {
        return prefs.getString("currency_code", "NPR") ?: "NPR"
    }

    fun setCurrency(currency: String) {
        prefs.edit { putString("currency_code", currency) }
    }

    fun isBackupEnabled(): Boolean {
        return prefs.getBoolean("backup_enabled", true)
    }

    fun setBackupEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("backup_enabled", enabled) }
    }

    fun getLastSyncTime(): Long? {
        val time = prefs.getLong("last_sync_time", -1)
        return if (time == -1L) null else time
    }

    fun setLastSyncTime(time: Long) {
        prefs.edit { putLong("last_sync_time", time) }
    }

    fun getAutoSyncInterval(): Long {
        return prefs.getLong("auto_sync_interval", 24 * 60 * 60 * 1000) // 24 hours default
    }

    fun setAutoSyncInterval(interval: Long) {
        prefs.edit { putLong("auto_sync_interval", interval) }
    }
}