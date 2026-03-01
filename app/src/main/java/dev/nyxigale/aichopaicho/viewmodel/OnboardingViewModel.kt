package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.viewmodel.data.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Load initial values from prefs if any (though usually defaults for new users)
        _uiState.update {
            it.copy(
                languageCode = AppPreferenceUtils.getLanguageCode(context),
                currencyCode = AppPreferenceUtils.getCurrencyCode(context),
                analyticsEnabled = AppPreferenceUtils.isAnalyticsEnabled(context)
            )
        }
    }

    fun setLanguage(langCode: String) {
        _uiState.update { it.copy(languageCode = langCode) }
        AppPreferenceUtils.setLanguageCode(context, langCode)
    }

    fun setCurrency(currencyCode: String) {
        _uiState.update { it.copy(currencyCode = currencyCode) }
        AppPreferenceUtils.setCurrencyCode(context, currencyCode)
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(analyticsEnabled = enabled) }
        AppPreferenceUtils.setAnalyticsEnabled(context, enabled)
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(notificationPermissionGranted = granted) }
    }

    fun nextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
        }
    }

    fun completeOnboarding() {
        AppPreferenceUtils.setOnboardingCompleted(context, true)
        _uiState.update { it.copy(isOnboardingCompleted = true) }
    }
}
