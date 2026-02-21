package dev.nyxigale.aichopaicho.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nyxigale.aichopaicho.AppLocaleManager
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.BackgroundSyncWorker
import dev.nyxigale.aichopaicho.data.DueReminderWorker
import dev.nyxigale.aichopaicho.data.entity.User
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.CsvTransferService
import dev.nyxigale.aichopaicho.data.repository.PreferencesRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.SyncCenterRepository
import dev.nyxigale.aichopaicho.data.repository.SyncRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.data.sync.SyncReport
import dev.nyxigale.aichopaicho.viewmodel.data.SettingsUiState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.core.content.pm.PackageInfoCompat
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
    private val syncCenterRepository: SyncCenterRepository,
    private val preferencesRepository: PreferencesRepository,
    private val csvTransferService: CsvTransferService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val workManager: WorkManager = WorkManager.getInstance(context)

    init {
        loadInitialData()
        observeSyncCenterState()
        observeWorkProgress()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser()
                val languageCode = AppPreferenceUtils.getLanguageCode(context)
                val currencies = Currency.getAvailableCurrencies().map { it.currencyCode }
                val selectedCurrency = preferencesRepository.getCurrency()
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: context.getString(R.string.Version_number)
                val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toString()

                _uiState.value = _uiState.value.copy(
                    user = user,
                    appVersion = versionName,
                    buildNumber = versionCode,
                    lastSyncTime = preferencesRepository.getLastSyncTime(),
                    availableCurrencies = currencies,
                    selectedCurrency = selectedCurrency,
                    selectedLanguage = languageCode,
                    isBackupEnabled = preferencesRepository.isBackupEnabled(),
                    isDueReminderEnabled = preferencesRepository.isDueReminderEnabled(),
                    requiresNotificationPermissionPrompt = requiresRuntimeNotificationPermission(),
                    hasNotificationPermission = hasNotificationPermission(),
                    isHideAmountsEnabled = preferencesRepository.isHideAmountsEnabled(),
                    isAnalyticsEnabled = preferencesRepository.isAnalyticsEnabled()
                )
            } catch (error: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_load_settings, error.message))
            }
        }
    }

    private fun observeSyncCenterState() {
        viewModelScope.launch {
            syncCenterRepository.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isSyncing = state.isSyncing,
                    syncProgress = state.currentProgress / 100f,
                    syncMessage = state.currentStage.ifBlank {
                        state.lastSyncMessage.orEmpty()
                    },
                    lastSyncTime = state.lastSyncTime ?: _uiState.value.lastSyncTime,
                    syncQueuedCount = state.queuedCount,
                    syncSuccessCount = state.successCount,
                    syncFailedCount = state.failedCount,
                    hasFailedSyncItems = state.failedItems.isNotEmpty()
                )
            }
        }
    }

    private fun observeWorkProgress() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(BackgroundSyncWorker.ONE_TIME_SYNC_WORK_NAME)
                .collect { infos ->
                    val info = infos.firstOrNull() ?: return@collect
                    val progress = info.progress.getInt(BackgroundSyncWorker.PROGRESS_PERCENT, -1)
                    val stage = info.progress.getString(BackgroundSyncWorker.PROGRESS_STAGE).orEmpty()

                    if (progress >= 0) {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED,
                            syncProgress = progress / 100f,
                            syncMessage = stage.ifBlank { _uiState.value.syncMessage }
                        )
                    }

                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        _uiState.value = _uiState.value.copy(lastSyncTime = preferencesRepository.getLastSyncTime())
                    }
                }
        }
    }

    fun loadCurrencySettings(context: Context) {
        val currentCurrency = AppPreferenceUtils.getCurrencyCode(context)
        _uiState.value = _uiState.value.copy(selectedCurrency = currentCurrency)
    }

    fun updateLanguage(activity: Activity, language: String) {
        AppPreferenceUtils.setLanguageCode(context, language)
        AppLocaleManager.setAppLocale(context, language)
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
        viewModelScope.launch {
            preferencesRepository.setBackupEnabled(enabled)
            if (enabled) {
                BackgroundSyncWorker.schedulePeriodicSync(context)
            } else {
                BackgroundSyncWorker.cancelSync(context)
                syncCenterRepository.markIdle("Backup disabled")
            }
        }
    }

    fun toggleBackupEnabled() {
        updateBackupEnabled(!_uiState.value.isBackupEnabled)
    }

    fun toggleDueReminderEnabled() {
        val enabled = !_uiState.value.isDueReminderEnabled
        _uiState.value = _uiState.value.copy(isDueReminderEnabled = enabled)
        viewModelScope.launch {
            preferencesRepository.setDueReminderEnabled(enabled)
            if (enabled) {
                if (requiresRuntimeNotificationPermission() && !hasNotificationPermission()) {
                    setErrorMessage(context.getString(R.string.notifications_permission_required_message))
                }
                DueReminderWorker.schedulePeriodic(context)
            } else {
                DueReminderWorker.cancel(context)
            }
        }
    }

    fun refreshNotificationPermissionStatus() {
        _uiState.value = _uiState.value.copy(
            requiresNotificationPermissionPrompt = requiresRuntimeNotificationPermission(),
            hasNotificationPermission = hasNotificationPermission()
        )
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasNotificationPermission = granted)
        if (granted) {
            if (_uiState.value.isDueReminderEnabled) {
                DueReminderWorker.schedulePeriodic(context)
            }
            setErrorMessage(null)
        } else if (_uiState.value.isDueReminderEnabled && requiresRuntimeNotificationPermission()) {
            setErrorMessage(context.getString(R.string.notification_permission_denied_message))
        }
    }

    fun toggleHideAmountsEnabled() {
        val enabled = !_uiState.value.isHideAmountsEnabled
        _uiState.value = _uiState.value.copy(isHideAmountsEnabled = enabled)
        viewModelScope.launch {
            preferencesRepository.setHideAmountsEnabled(enabled)
        }
    }

    fun toggleAnalyticsEnabled() {
        val enabled = !_uiState.value.isAnalyticsEnabled
        _uiState.value = _uiState.value.copy(isAnalyticsEnabled = enabled)
        viewModelScope.launch {
            preferencesRepository.setAnalyticsEnabled(enabled)
        }
    }

    fun showSignInDialog() {
        _uiState.value = _uiState.value.copy(showSignInDialog = true)
    }

    fun selectCurrency(currency: String, context: Context) {
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

    fun showDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = true)
    }

    fun hideDeleteAccountDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
    }

    suspend fun signInWithGoogle(activity: Activity, isReturningUser: Boolean = false) {
        try {
            setErrorMessage(null)

            firebaseAuth.currentUser?.let { currentUser ->
                val localUser = userRepository.getUser()
                if (localUser.id == currentUser.uid) {
                    hideSignInDialog()
                    return
                }
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(isReturningUser)
                .setServerClientId(activity.getString(R.string.web_client))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(request = request, context = activity)
            val firebaseUser = handleSignIn(result)

            firebaseUser?.let { signedInFirebaseUser ->
                val resolvedUser = resolveSignedInUser(signedInFirebaseUser)
                transferLocalDataToFirebaseUser(resolvedUser)
                hideSignInDialog()
            }

            BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(activity.applicationContext)
        } catch (error: Exception) {
            Log.e("SettingsViewModel", "Sign in failed", error)
            setErrorMessage(error.message ?: context.getString(R.string.sign_in_failed))
            hideSignInDialog()
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): FirebaseUser? {
        val credential = result.credential

        return when (credential.type) {
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                if (idToken.isEmpty()) {
                    throw Exception(context.getString(R.string.failed_to_retrieve_google_id_token))
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                authResult.user
            }

            else -> {
                throw IllegalArgumentException(
                    context.getString(R.string.unsupported_credential_type, credential.type)
                )
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

    private suspend fun resolveSignedInUser(firebaseUser: FirebaseUser): User {
        return when (val resolution = userRepository.resolveUserForSignIn(firebaseUser)) {
            is UserRepository.SignInResolution.Allowed -> {
                userRepository.upsertRemoteUser(resolution.user)
                resolution.user
            }

            is UserRepository.SignInResolution.RecoveryWindowExpired -> {
                firebaseAuth.signOut()
                throw IllegalStateException(context.getString(R.string.account_recovery_window_expired))
            }
        }
    }

    private suspend fun transferLocalDataToFirebaseUser(resolvedUser: User) {
        val currentUser = _uiState.value.user
        userRepository.upsert(resolvedUser)

        if (currentUser != null && currentUser.id != resolvedUser.id) {
            updateUserIdAcrossAllTables(currentUser.id, resolvedUser.id)
            userRepository.deleteUserCompletely(currentUser.id)
        }

        _uiState.value = _uiState.value.copy(user = resolvedUser)
        startSync()
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val currentUser = _uiState.value.user
                currentUser?.let { user ->
                    userRepository.deleteUserCompletely(user.id)
                    firebaseAuth.signOut()

                    val newOfflineUser = createNewOfflineUser()
                    userRepository.upsert(newOfflineUser)

                    _uiState.value = _uiState.value.copy(
                        user = newOfflineUser,
                        showSignOutDialog = false
                    )
                }
            } catch (error: Exception) {
                setErrorMessage(context.getString(R.string.sign_out_failed, error.message))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val currentUser = _uiState.value.user
            if (currentUser == null || currentUser.isOffline) {
                setErrorMessage(context.getString(R.string.account_delete_requires_sign_in))
                _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
                return@launch
            }

            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val deletedUser = currentUser.copy(
                    isDeleted = true,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.upsertRemoteUser(deletedUser)
                userRepository.deleteUserCompletely(currentUser.id)
                firebaseAuth.signOut()

                val offlineUser = createNewOfflineUser()
                userRepository.upsert(offlineUser)

                _uiState.value = _uiState.value.copy(
                    user = offlineUser,
                    showDeleteAccountDialog = false,
                    showSignOutDialog = false
                )
            } catch (error: Exception) {
                setErrorMessage(context.getString(R.string.account_delete_failed, error.message))
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
        BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(context)
    }

    fun retryFailedSyncItems() {
        viewModelScope.launch {
            val failedItems = syncCenterRepository.state.value.failedItems
            if (failedItems.isEmpty()) {
                setErrorMessage(context.getString(R.string.no_failed_items_to_retry))
                return@launch
            }

            try {
                syncCenterRepository.beginSync(failedItems.size)
                val report = syncRepository.retryFailedItems(failedItems)
                val finishedAt = System.currentTimeMillis()
                if (report.failed == 0) {
                    preferencesRepository.setLastSyncTime(finishedAt)
                }
                syncCenterRepository.completeSync(report, finishedAt)
                _uiState.value = _uiState.value.copy(lastSyncTime = preferencesRepository.getLastSyncTime())
            } catch (error: Exception) {
                syncCenterRepository.markSyncFailed(SyncReport.EMPTY, error.message)
            }
        }
    }

    fun exportCsvData(folderUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCsvOperationRunning = true, csvOperationMessage = null)
            val result = csvTransferService.exportCsvBundleToFolder(folderUri)
            _uiState.value = _uiState.value.copy(
                isCsvOperationRunning = false,
                csvOperationMessage = result.message,
                csvOperationLocation = result.location
            )
        }
    }

    fun importCsvData(fileUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCsvOperationRunning = true, csvOperationMessage = null)
            val result = csvTransferService.importFromCsvFile(fileUri)
            _uiState.value = _uiState.value.copy(
                isCsvOperationRunning = false,
                csvOperationMessage = result.message,
                csvOperationLocation = result.location
            )
        }
    }

    private fun setErrorMessage(message: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun updateUserIdAcrossAllTables(id: String, firebaseUid: String) {
        recordRepository.updateUserId(id, firebaseUid)
        contactRepository.updateUserId(id, firebaseUid)
    }

    private fun requiresRuntimeNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    private fun hasNotificationPermission(): Boolean {
        if (!requiresRuntimeNotificationPermission()) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
