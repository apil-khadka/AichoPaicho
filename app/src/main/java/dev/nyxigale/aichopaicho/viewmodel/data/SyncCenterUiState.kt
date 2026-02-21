package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.sync.SyncFailureItem

data class SyncCenterUiState(
    val isSyncing: Boolean = false,
    val progress: Float = 0f,
    val stage: String = "",
    val queuedCount: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val failedItems: List<SyncFailureItem> = emptyList(),
    val lastSyncTime: Long? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)
