package dev.nyxigale.aichopaicho.viewmodel.data

data class OnboardingUiState(
    val currentPage: Int = 0,
    val languageCode: String = "en",
    val currencyCode: String = "NPR",
    val analyticsEnabled: Boolean = true,
    val notificationPermissionGranted: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)
