package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.User

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val selectedCurrency: String = "NPR",
    val selectedLanguage: String = "en",
    val isBackupEnabled: Boolean = true,
    val isDueReminderEnabled: Boolean = true,
    val requiresNotificationPermissionPrompt: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val isHideAmountsEnabled: Boolean = false,
    val isAnalyticsEnabled: Boolean = true,
    val availableCurrencies: List<String>  = emptyList(),
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f,
    val syncMessage: String = "",
    val syncQueuedCount: Int = 0,
    val syncSuccessCount: Int = 0,
    val syncFailedCount: Int = 0,
    val hasFailedSyncItems: Boolean = false,
    val showSignInDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showCurrencyDropdown: Boolean = false,
    val appVersion: String = "",
    val lastSyncTime: Long? = null,
    val buildNumber: String = "",
    val isCsvOperationRunning: Boolean = false,
    val csvOperationMessage: String? = null,
    val csvOperationLocation: String? = null
)
