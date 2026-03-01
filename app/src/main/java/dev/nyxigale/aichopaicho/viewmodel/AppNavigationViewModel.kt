package dev.nyxigale.aichopaicho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.data.local.ScreenViewRepository
import dev.nyxigale.aichopaicho.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    val screenViewRepository: ScreenViewRepository
) : ViewModel(){

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    fun refreshStartDestination() {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val onboardingCompleted = screenViewRepository.getScreenView(Routes.ONBOARDING_SCREEN) == true
            if (!onboardingCompleted) {
                _startDestination.value = Routes.ONBOARDING_SCREEN
            } else if (screenViewRepository.getScreenView(Routes.WELCOME_SCREEN) == true) {
                _startDestination.value = Routes.DASHBOARD_SCREEN
            } else {
                _startDestination.value = Routes.WELCOME_SCREEN
            }
        }
    }
}
