package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.BackgroundSyncWorker
import dev.nyxigale.aichopaicho.data.repository.PreferencesRepository
import dev.nyxigale.aichopaicho.data.repository.SyncCenterRepository
import dev.nyxigale.aichopaicho.data.repository.SyncRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.data.sync.SyncReport
import dev.nyxigale.aichopaicho.viewmodel.data.SyncCenterUiState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SyncCenterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncCenterRepository: SyncCenterRepository,
    private val syncRepository: SyncRepository,
    private val preferencesRepository: PreferencesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncCenterUiState())
    val uiState: StateFlow<SyncCenterUiState> = _uiState.asStateFlow()

    init {
        observeSyncCenter()
    }

    private fun observeSyncCenter() {
        viewModelScope.launch {
            syncCenterRepository.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isSyncing = state.isSyncing,
                    progress = state.currentProgress / 100f,
                    stage = state.currentStage,
                    queuedCount = state.queuedCount,
                    successCount = state.successCount,
                    failedCount = state.failedCount,
                    failedItems = state.failedItems,
                    lastSyncTime = state.lastSyncTime,
                    statusMessage = state.lastSyncMessage
                )
            }
        }
    }

    fun startSync() {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user.isOffline || !preferencesRepository.isBackupEnabled()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = context.getString(R.string.sync_requires_signed_in_backup_enabled)
                )
                return@launch
            }
            BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(context)
        }
    }

    fun retryFailedItems() {
        viewModelScope.launch {
            val failedItems = syncCenterRepository.state.value.failedItems
            if (failedItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = context.getString(R.string.no_failed_items_to_retry)
                )
                return@launch
            }

            try {
                syncCenterRepository.beginSync(failedItems.size)
                val report = syncRepository.retryFailedItems(failedItems)
                val finishedAt = System.currentTimeMillis()
                if (report.failed == 0) {
                    preferencesRepository.setLastSyncTime(finishedAt)
                }
                syncCenterRepository.completeSync(report, finishedAt)
            } catch (error: Exception) {
                syncCenterRepository.markSyncFailed(
                    partialReport = SyncReport.EMPTY,
                    reason = error.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
