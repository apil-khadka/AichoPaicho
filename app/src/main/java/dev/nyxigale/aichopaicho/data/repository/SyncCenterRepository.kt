package dev.nyxigale.aichopaicho.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nyxigale.aichopaicho.data.sync.SyncCenterState
import dev.nyxigale.aichopaicho.data.sync.SyncFailureItem
import dev.nyxigale.aichopaicho.data.sync.SyncReport
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SyncCenterRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sync_center_preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<SyncCenterState> = _state.asStateFlow()

    fun beginSync(queuedCount: Int) {
        val current = _state.value
        saveState(
            current.copy(
                isSyncing = true,
                queuedCount = queuedCount,
                successCount = 0,
                failedCount = 0,
                failedItems = emptyList(),
                currentProgress = 0,
                currentStage = "Preparing sync",
                lastSyncMessage = null
            )
        )
    }

    fun updateInProgress(progress: Int, stage: String) {
        val current = _state.value
        saveState(
            current.copy(
                isSyncing = true,
                currentProgress = progress.coerceIn(0, 100),
                currentStage = stage
            )
        )
    }

    fun completeSync(report: SyncReport, finishedAt: Long = System.currentTimeMillis()) {
        val message = if (report.failed == 0) {
            "Sync completed successfully"
        } else {
            "Sync completed with ${report.failed} failed item(s)"
        }
        saveState(
            SyncCenterState(
                isSyncing = false,
                queuedCount = report.attempted,
                successCount = report.succeeded,
                failedCount = report.failed,
                failedItems = report.failedItems.distinctBy { it.key() },
                currentProgress = 100,
                currentStage = "Completed",
                lastSyncTime = finishedAt,
                lastSyncMessage = message
            )
        )
    }

    fun markSyncFailed(partialReport: SyncReport, reason: String?) {
        val failureItem = SyncFailureItem(
            entityType = dev.nyxigale.aichopaicho.data.sync.SyncEntityType.USER,
            entityId = "sync-run",
            reason = reason
        )
        val mergedFailedItems = (partialReport.failedItems + failureItem).distinctBy { it.key() }
        saveState(
            SyncCenterState(
                isSyncing = false,
                queuedCount = partialReport.attempted + 1,
                successCount = partialReport.succeeded,
                failedCount = partialReport.failed + 1,
                failedItems = mergedFailedItems,
                currentProgress = 100,
                currentStage = "Failed",
                lastSyncTime = _state.value.lastSyncTime,
                lastSyncMessage = reason ?: "Sync failed"
            )
        )
    }

    fun markIdle(message: String? = null) {
        val current = _state.value
        saveState(
            current.copy(
                isSyncing = false,
                currentProgress = 0,
                currentStage = "",
                lastSyncMessage = message ?: current.lastSyncMessage
            )
        )
    }

    private fun readState(): SyncCenterState {
        val failedItemsJson = prefs.getString(KEY_FAILED_ITEMS, null)
        val failedItems = if (failedItemsJson.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching {
                val type = object : TypeToken<List<SyncFailureItem>>() {}.type
                gson.fromJson<List<SyncFailureItem>>(failedItemsJson, type) ?: emptyList()
            }.getOrElse { emptyList() }
        }

        return SyncCenterState(
            isSyncing = prefs.getBoolean(KEY_IS_SYNCING, false),
            queuedCount = prefs.getInt(KEY_QUEUED_COUNT, 0),
            successCount = prefs.getInt(KEY_SUCCESS_COUNT, 0),
            failedCount = prefs.getInt(KEY_FAILED_COUNT, 0),
            failedItems = failedItems,
            currentProgress = prefs.getInt(KEY_CURRENT_PROGRESS, 0),
            currentStage = prefs.getString(KEY_CURRENT_STAGE, "") ?: "",
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, -1L).takeIf { it > 0 },
            lastSyncMessage = prefs.getString(KEY_LAST_SYNC_MESSAGE, null)
        )
    }

    private fun saveState(state: SyncCenterState) {
        val failedItemsJson = gson.toJson(state.failedItems)
        prefs.edit {
            putBoolean(KEY_IS_SYNCING, state.isSyncing)
            putInt(KEY_QUEUED_COUNT, state.queuedCount)
            putInt(KEY_SUCCESS_COUNT, state.successCount)
            putInt(KEY_FAILED_COUNT, state.failedCount)
            putString(KEY_FAILED_ITEMS, failedItemsJson)
            putInt(KEY_CURRENT_PROGRESS, state.currentProgress)
            putString(KEY_CURRENT_STAGE, state.currentStage)
            if (state.lastSyncTime != null) {
                putLong(KEY_LAST_SYNC_TIME, state.lastSyncTime)
            } else {
                remove(KEY_LAST_SYNC_TIME)
            }
            putString(KEY_LAST_SYNC_MESSAGE, state.lastSyncMessage)
        }
        _state.value = state
    }

    companion object {
        private const val KEY_IS_SYNCING = "is_syncing"
        private const val KEY_QUEUED_COUNT = "queued_count"
        private const val KEY_SUCCESS_COUNT = "success_count"
        private const val KEY_FAILED_COUNT = "failed_count"
        private const val KEY_FAILED_ITEMS = "failed_items"
        private const val KEY_CURRENT_PROGRESS = "current_progress"
        private const val KEY_CURRENT_STAGE = "current_stage"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_MESSAGE = "last_sync_message"
    }
}
