package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun updateLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun toggleBackupEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isBackupEnabled = enabled) }
    }

    fun startSync() {
        _uiState.update { it.copy(isSyncing = true) }
        // Simulate sync
        _uiState.update { it.copy(isSyncing = false, lastSyncTime = System.currentTimeMillis()) }
    }

    fun showSignInDialog() {
        _uiState.update { it.copy(showSignInDialog = true) }
    }

    fun showSignOutDialog() {
        _uiState.update { it.copy(showSignOutDialog = true) }
    }

    fun hideSignOutDialog() {
        _uiState.update { it.copy(showSignOutDialog = false) }
    }

    fun toggleCurrencyDropdown(show: Boolean) {
        _uiState.update { it.copy(showCurrencyDropdown = show) }
    }

    fun selectCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency, showCurrencyDropdown = false) }
    }

    fun signInWithGoogle(activity: android.app.Activity) {
        // Placeholder
    }

    fun signOut() {
        // Placeholder
    }
}
