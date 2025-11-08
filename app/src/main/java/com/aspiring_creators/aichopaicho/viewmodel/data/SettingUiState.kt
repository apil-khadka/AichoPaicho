package com.aspiring_creators.aichopaicho.viewmodel.data

import android.view.textclassifier.TextLanguage
import com.aspiring_creators.aichopaicho.data.entity.User

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val selectedCurrency: String = "NPR",
    val selectedLanguage: String = "en",
    val isBackupEnabled: Boolean = true,
    val availableCurrencies: List<String>  = emptyList(),
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f,
    val syncMessage: String = "",
    val showSignInDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showCurrencyDropdown: Boolean = false,
    val appVersion: String = "",
    val lastSyncTime: Long? = null,
    val buildNumber: String = ""
)