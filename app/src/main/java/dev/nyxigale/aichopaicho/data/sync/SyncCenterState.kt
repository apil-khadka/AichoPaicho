package dev.nyxigale.aichopaicho.data.sync

data class SyncCenterState(
    val isSyncing: Boolean = false,
    val queuedCount: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val failedItems: List<SyncFailureItem> = emptyList(),
    val currentProgress: Int = 0,
    val currentStage: String = "",
    val lastSyncTime: Long? = null,
    val lastSyncMessage: String? = null
)
