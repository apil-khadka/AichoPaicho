package dev.nyxigale.aichopaicho

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import dev.nyxigale.aichopaicho.data.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AichoPaichoApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        private const val TAG = "AichoPaichoApp"
        private const val CLARITY_PROJECT_ID = "vkq46hwvk6"
        private const val APP_PREFS_NAME = "app_preferences"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    }



    // This is the property that Configuration.Provider requires
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Clarity requires initialization after WorkManager when using custom WorkManager setup.
        WorkManager.getInstance(applicationContext)
        NotificationChannels.createAll(applicationContext)

        val clarityInitialized = if (isAnalyticsEnabled()) {
            Clarity.initialize(
                applicationContext,
                ClarityConfig(projectId = CLARITY_PROJECT_ID)
            )
        } else {
            false
        }

        Log.d(TAG, "WorkManager configuration initialized with HiltWorkerFactory: $workerFactory")
        Log.i(TAG, "Microsoft Clarity initialized: $clarityInitialized")
    }

    private fun isAnalyticsEnabled(): Boolean {
        val prefs = getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(KEY_ANALYTICS_ENABLED, true)
    }
}
