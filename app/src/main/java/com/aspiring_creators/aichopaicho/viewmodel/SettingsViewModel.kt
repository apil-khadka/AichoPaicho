package com.aspiring_creators.aichopaicho.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat.recreate
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.AppLocaleManager
import com.aspiring_creators.aichopaicho.AppPreferenceUtils
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.BackgroundSyncWorker
import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.PreferencesRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.data.repository.SyncRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.SettingsUiState
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Currency
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository,
    private val firebaseAuth: FirebaseAuth,
    private val syncRepository: SyncRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val context: Context

) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser()
                val appVersion = getAppVersion()
                val lastSync = getLastSyncTime()
                val languageCode = AppPreferenceUtils.getLanguageCode(context)
                val currencies = Currency.getAvailableCurrencies().map{it.currencyCode}
               val selectedCurrency: String =  preferencesRepository.getCurrency()
                _uiState.value = _uiState.value.copy(
                    user = user,
                    appVersion = appVersion,
                    lastSyncTime = lastSync ,
                    availableCurrencies = currencies,
                    selectedCurrency = selectedCurrency,
                    selectedLanguage = languageCode
                )

            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_load_settings, e.message))
            }
        }
    }

    fun loadCurrencySettings(context: Context) {
        val currentCurrency = AppPreferenceUtils.getCurrencyCode(context)
        _uiState.value = _uiState.value.copy(selectedCurrency = currentCurrency)
    }

    fun updateLanguage(activity: Activity,language: String){
        AppPreferenceUtils.setLanguageCode(context, language)
        AppLocaleManager.setAppLocale(context , language)
        _uiState.value = _uiState.value.copy(selectedLanguage = language)

        recreate(activity)
    }

    fun updateCurrency(context: Context, currencyCode: String) {
        AppPreferenceUtils.setCurrencyCode(context, currencyCode)
        _uiState.value = _uiState.value.copy(
            selectedCurrency = currencyCode,
            showCurrencyDropdown = false
        )
    }

    fun toggleCurrencyDropdown() {
        _uiState.value = _uiState.value.copy(
            showCurrencyDropdown = !_uiState.value.showCurrencyDropdown
        )
    }

    fun updateBackupEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isBackupEnabled = enabled)
        // Save to preferences
        viewModelScope.launch {
            saveBackupPreference(enabled)
        }
    }
    fun toggleBackupEnabled() {
        val newBackupEnabled = !_uiState.value.isBackupEnabled
        updateBackupEnabled(newBackupEnabled)
    }

    fun showSignInDialog() {
        _uiState.value = _uiState.value.copy(showSignInDialog = true)
    }
    fun selectCurrency(currency:String, context: Context){
        _uiState.value = _uiState.value.copy(
            selectedCurrency = currency,
            showCurrencyDropdown = false
        )
        AppPreferenceUtils.setCurrencyCode(context, currency)
    }

    fun hideSignInDialog() {
        _uiState.value = _uiState.value.copy(showSignInDialog = false)
    }

    fun showSignOutDialog() {
        _uiState.value = _uiState.value.copy(showSignOutDialog = true)
    }

    fun hideSignOutDialog() {
        _uiState.value = _uiState.value.copy(showSignOutDialog = false)
    }


    suspend fun signInWithGoogle(activity: Activity, isReturningUser: Boolean = false): Unit {
         try {
            setErrorMessage("")

            // Check if already signed in
            firebaseAuth.currentUser?.let { currentUser ->
                val localUser = userRepository.getUser()
                if (localUser.id == currentUser.uid) {
                    run {
                         Result.success(currentUser)
                    }
                }
            }

            // Perform Google Sign In
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(isReturningUser)
                .setServerClientId(activity.getString(R.string.web_client))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.Companion.create(activity)
            val result = credentialManager.getCredential(request = request, context = activity)

            val firebaseUser = handleSignIn(result)

            // Save user to database
            firebaseUser?.let {

                transferLocalDataToFirebaseUser(firebaseUser)
                hideSignInDialog()
            }

             BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(activity.applicationContext)

            Result.success(firebaseUser!!)

        } catch (e: Exception) {
            Log.e("SettingViewModel", "Sign in failed", e)
            setErrorMessage(e.message ?: context.getString(R.string.sign_in_failed))
            Result.failure(e)
        } finally {
        }
    }
    private suspend fun handleSignIn(result: GetCredentialResponse): FirebaseUser? {
        val credential = result.credential

        when (credential.type) {
            GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                if (idToken.isEmpty()) {
                    throw Exception(context.getString(R.string.failed_to_retrieve_google_id_token))
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                return authResult.user
            }
            else -> {
                throw IllegalArgumentException(
                    context.getString(
                        R.string.unsupported_credential_type,
                        credential.type
                    ))
            }
        }
    }

    private suspend fun <T> Task<T>.await(): T {
        return suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.exception != null) {
                    cont.resumeWithException(task.exception!!)
                } else {
                    cont.resume(task.result)
                }
            }
        }
    }

    private suspend fun transferLocalDataToFirebaseUser(firebaseUser: FirebaseUser) {
        try {
            val currentUser = _uiState.value.user
            currentUser?.let { user ->
                // Update user with Firebase UID
                val updatedUser = user.copy(
                    id = firebaseUser.uid,
                    isOffline = false,
                    name = firebaseUser.displayName,
                    email = firebaseUser.email,
                    photoUrl = firebaseUser.photoUrl
                )

                // Update all related data with new user ID
                userRepository.upsert(updatedUser)
                updateUserIdAcrossAllTables(user.id, firebaseUser.uid)
                userRepository.deleteUserCompletely(user.id)


                _uiState.value = _uiState.value.copy(user = updatedUser)

                startSync()
            }
        } catch (e: Exception) {
            setErrorMessage(context.getString(R.string.data_transfer_failed, e.message))
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val currentUser = _uiState.value.user
                currentUser?.let { user ->
                    // Delete all user data (hard delete)
                    userRepository.deleteUserCompletely(user.id)

                    // Sign out from Firebase
                    firebaseAuth.signOut()

                    // Create new offline user
                    val newOfflineUser = createNewOfflineUser()
//                    userRepository.upsert(newOfflineUser)

                    _uiState.value = _uiState.value.copy(
                        user = newOfflineUser,
                        showSignOutDialog = false
                    )
                }
            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.sign_out_failed, e.message))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun createNewOfflineUser(): User {
        return User(
            id = UUID.randomUUID().toString(),
            name = null,
            email = null,
            photoUrl = null,
            isOffline = true
        )
    }

    fun startSync() {
        if (!_uiState.value.isBackupEnabled || _uiState.value.user?.isOffline == true) {
            return
        }
        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncProgress = 0f,
            syncMessage = context.getString(R.string.starting_backup)
        )
        BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(context)
        _uiState.value = _uiState.value.copy(
            syncProgress = 0.25f,
            syncMessage = context.getString(R.string.backing_up_contacts)
        )

        _uiState.value = _uiState.value.copy(
            syncProgress = 1f,
            syncMessage = context.getString(R.string.backup_completed_successfully)
        )
    }
    private fun getAppVersion(): String {
        // You'll implement this to get actual app version
        return context.getString(R.string.Version_number)
    }

    private suspend fun getLastSyncTime(): Long? {
       return preferencesRepository.getLastSyncTime()
    }

    private suspend fun saveLastSyncTime(timestamp: Long) {
        preferencesRepository.setLastSyncTime(timestamp)
    }

    private suspend fun saveBackupPreference(enabled: Boolean) {
       preferencesRepository.setBackupEnabled(enabled)
    }

    private fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun SettingsViewModel.updateUserIdAcrossAllTables(id: String, firebaseUid: String) {
        recordRepository.updateUserId(id, firebaseUid)
        contactRepository.updateUserId(id, firebaseUid)
    }
}