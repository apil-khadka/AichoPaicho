package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isSecurityEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val hasPin: Boolean = false,
    val enteredPin: String = "",
    val setupStep: SecuritySetupStep = SecuritySetupStep.NONE,
    val tempPin: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

enum class SecuritySetupStep {
    NONE,
    ENTER_NEW_PIN,
    CONFIRM_NEW_PIN,
    SUCCESS
}

@HiltViewModel
class SecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    private fun refreshState() {
        _uiState.update {
            it.copy(
                isSecurityEnabled = AppPreferenceUtils.isSecurityEnabled(context),
                isBiometricEnabled = AppPreferenceUtils.isBiometricEnabled(context),
                hasPin = AppPreferenceUtils.getPin(context) != null
            )
        }
    }

    fun onPinInput(digit: String) {
        if (_uiState.value.enteredPin.length < 4) {
            _uiState.update { it.copy(enteredPin = it.enteredPin + digit, error = null) }
            
            if (_uiState.value.enteredPin.length == 4) {
                verifyPin()
            }
        }
    }

    fun onPinDelete() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = it.enteredPin.dropLast(1)) }
        }
    }

    private fun verifyPin() {
        val storedPin = AppPreferenceUtils.getPin(context)
        if (_uiState.value.enteredPin == storedPin) {
            _uiState.update { it.copy(isAuthenticated = true, error = null) }
        } else {
            _uiState.update { it.copy(enteredPin = "", error = "Incorrect PIN") }
        }
    }

    fun startSetup() {
        _uiState.update { it.copy(setupStep = SecuritySetupStep.ENTER_NEW_PIN, enteredPin = "") }
    }

    fun onSetupPinInput(digit: String) {
        if (_uiState.value.enteredPin.length < 4) {
            _uiState.update { it.copy(enteredPin = it.enteredPin + digit) }
            
            if (_uiState.value.enteredPin.length == 4) {
                handleSetupStep()
            }
        }
    }

    private fun handleSetupStep() {
        val currentState = _uiState.value
        when (currentState.setupStep) {
            SecuritySetupStep.ENTER_NEW_PIN -> {
                _uiState.update { 
                    it.copy(
                        tempPin = it.enteredPin,
                        enteredPin = "",
                        setupStep = SecuritySetupStep.CONFIRM_NEW_PIN
                    )
                }
            }
            SecuritySetupStep.CONFIRM_NEW_PIN -> {
                if (currentState.enteredPin == currentState.tempPin) {
                    AppPreferenceUtils.setPin(context, currentState.enteredPin)
                    AppPreferenceUtils.setSecurityEnabled(context, true)
                    _uiState.update { it.copy(setupStep = SecuritySetupStep.SUCCESS, enteredPin = "") }
                    refreshState()
                } else {
                    _uiState.update { it.copy(enteredPin = "", error = "PINs do not match. Try again.") }
                }
            }
            else -> {}
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        AppPreferenceUtils.setBiometricEnabled(context, enabled)
        refreshState()
    }

    fun disableSecurity() {
        AppPreferenceUtils.setSecurityEnabled(context, false)
        AppPreferenceUtils.setPin(context, null)
        AppPreferenceUtils.setBiometricEnabled(context, false)
        refreshState()
    }

    fun authenticateWithBiometric(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _uiState.update { it.copy(isAuthenticated = true) }
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Don't show error for user cancellation
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        _uiState.update { it.copy(error = errString.toString()) }
                    }
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _uiState.update { it.copy(error = "Authentication failed") }
                    onResult(false)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("SecurityViewModel", "Biometric auth failed to start", e)
            onResult(false)
        }
    }
    
    fun canUseBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
