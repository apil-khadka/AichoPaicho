package com.aspiring_creators.aichopaicho.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.mapper.toUserEntity
import com.aspiring_creators.aichopaicho.data.local.ScreenViewRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.ui.navigation.Routes
import com.aspiring_creators.aichopaicho.viewmodel.data.WelcomeScreenUiState
import com.aspiring_creators.aichopaicho.worker.BackgroundSyncWorker
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val screenViewRepository: ScreenViewRepository,
 ) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeScreenUiState())
    val uiState: StateFlow<WelcomeScreenUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            setUser(auth.currentUser)
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = value)
    }

    private fun setErrorMessage(value: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = value)
    }

    private fun setUser(firebaseUser: FirebaseUser?) {
        val user = firebaseUser?.toUserEntity()
        _uiState.value = _uiState.value.copy(user = user)
    }

    // Returns Result for UI to handle navigation
    suspend fun signInWithGoogle(activity: Activity, isReturningUser: Boolean = false): Result<FirebaseUser> {
        return try {
            setLoading(true)
            setErrorMessage(null)

            // Check if already signed in
            firebaseAuth.currentUser?.let { currentUser ->
                val localUser = userRepository.getUser()
                if (localUser.id == currentUser.uid) {
                    run {
                        screenViewRepository.markScreenAsShown(Routes.WELCOME_SCREEN)
                        return Result.success(currentUser)
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

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(request = request, context = activity)

            val user = handleSignIn(result)

            // Save user to database
            user?.let {
                userRepository.upsert(it.toUserEntity())
            }

            screenViewRepository.markScreenAsShown(Routes.WELCOME_SCREEN)
            BackgroundSyncWorker.scheduleOneTimeSyncOnLogin(activity.applicationContext)

            Result.success(user!!)

        } catch (e: Exception) {
            Log.e("WelcomeViewModel", "Sign in failed", e)
            setErrorMessage(e.message ?: context.getString(R.string.sign_in_failed))
            Result.failure(e)
        } finally {
            setLoading(false)
        }
    }

    suspend fun skipSignIn(): Result<Unit> {
        return try {
            viewModelScope.launch {
                userRepository.upsert(
                    User(
                        UUID.randomUUID().toString(),
                        null,
                        null,
                        null,
                        isOffline = true
                    )
                )
            }
            screenViewRepository.markScreenAsShown(Routes.WELCOME_SCREEN)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WelcomeViewModel", "Skip failed", e)
            Result.failure(e)
        }
    }

    fun clearError() {
        setErrorMessage(null)
    }

    // Helper method to check if user should auto-navigate
    suspend fun shouldAutoNavigate(): Boolean {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null ) {
            val localUser = userRepository.getUser()
            return localUser.id == currentUser.uid
            }
        return false
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): FirebaseUser? {
        val credential = result.credential

        when (credential.type) {
            TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                if (idToken.isEmpty()) {
                    throw Exception(context.getString(R.string.failed_to_retrieve_google_id_token))
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                setUser(authResult.user)
                return authResult.user
            }
            else -> {
                throw IllegalArgumentException(context.getString(
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
}
