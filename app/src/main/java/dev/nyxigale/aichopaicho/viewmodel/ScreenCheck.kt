package dev.nyxigale.aichopaicho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.data.local.ScreenViewRepository
import dev.nyxigale.aichopaicho.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScreenCheck @Inject constructor(private val screenViewRepository: ScreenViewRepository): ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val screenToCheck: List<String> = listOf(Routes.WELCOME_SCREEN, Routes.OFFLINE_INIT_SCREEN, Routes.PERMISSION_CONTACTS_SCREEN)

    // Private mutable state for multiple screens
    private val _screensShownStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    // Public read-only state
    val screensShownStatus: StateFlow<Map<String, Boolean>> = _screensShownStatus.asStateFlow()

    init {
        checkIfScreensHaveBeenShown()
    }

    private fun checkIfScreensHaveBeenShown() { // Renamed for clarity
        viewModelScope.launch {
            _isLoading.value = true
            val newStatusMap = mutableMapOf<String, Boolean>()
            for (screenId in screenToCheck) {
                val screenView = screenViewRepository.getScreenView(screenId)
                newStatusMap[screenId] = screenView == true
            }
            _screensShownStatus.value = newStatusMap // Update the StateFlow with the new map
            _isLoading.value = false
        }
    }

    fun markScreenAsShown(screenId: String) {
        viewModelScope.launch {
            screenViewRepository.markScreenAsShown(screenId)
            checkIfScreensHaveBeenShown()
        }
    }
}