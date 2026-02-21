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

        val clarityInitialized = Clarity.initialize(
            applicationContext,
            ClarityConfig(projectId = CLARITY_PROJECT_ID)
        )

        Log.d(TAG, "WorkManager configuration initialized with HiltWorkerFactory: $workerFactory")
        Log.i(TAG, "Microsoft Clarity initialized: $clarityInitialized")
    }
}
