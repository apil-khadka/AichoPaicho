package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.local.ScreenViewRepository
import com.aspiring_creators.aichopaicho.ui.navigation.Routes
import com.aspiring_creators.aichopaicho.viewmodel.data.PermissionScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class PermissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenViewRepository: ScreenViewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionScreenUiState())
    val uiState: StateFlow<PermissionScreenUiState> = _uiState.asStateFlow()

    private fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = value)
    }

    private fun setErrorMessage(value: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = value)
    }

    fun setPermissionGranted(value: Boolean) {
        _uiState.value = _uiState.value.copy(permissionGranted = value)
    }

    suspend fun grantPermissionAndProceed(): Result<Unit> {
        return try {
            setLoading(true)
            setErrorMessage(null)

            screenViewRepository.markScreenAsShown(Routes.PERMISSION_CONTACTS_SCREEN)
            Log.d("PermissionViewModel", "Permission granted, screen marked as shown")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PermissionViewModel", "Error marking permission screen as shown", e)
            setErrorMessage(context.getString(R.string.failed_to_save_permission_status))
            Result.failure(e)
        } finally {
            setLoading(false)
        }
    }

    suspend fun skipPermissionAndProceed(): Result<Unit> {
        return try {
            setLoading(true)
            setErrorMessage(null)

            screenViewRepository.markScreenAsShown(Routes.PERMISSION_CONTACTS_SCREEN)
            Log.d("PermissionViewModel", "Permission skipped, screen marked as shown")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PermissionViewModel", "Error marking permission screen as shown", e)
            setErrorMessage(context.getString(R.string.failed_to_save_permission_status))
            Result.failure(e)
        } finally {
            setLoading(false)
        }
    }

    fun clearError() {
        setErrorMessage(null)
    }
}