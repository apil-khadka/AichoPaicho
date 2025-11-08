package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.repository.UserRecordSummaryRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.DashboardScreenUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userRecordSummaryRepository: UserRecordSummaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardScreenUiState(isLoading = true))
    val uiState: StateFlow<DashboardScreenUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                Log.d("DashboardViewModel", "AuthStateListener: Firebase user signed in (UID: ${firebaseUser.uid}). Loading data.")
                loadUserDataAndDependents()
            } else {
                Log.d("DashboardViewModel", "AuthStateListener: Firebase user signed out. Clearing UI state.")
                _uiState.value = DashboardScreenUiState(isSignedIn = false) // Reset to a clean, signed-out state
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        // The listener will be called with the initial auth state.
        // If there's no current user, it will set isSignedIn to false.
        // If there is a user, it will call loadUserDataAndDependents().
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun loadUserDataAndDependents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    // Firebase reports a signed-in user.
                    val localUser = userRepository.getUser() // Fetch from local DB

                    // Crucial check: Does the local user match the Firebase session?
                    if (localUser.id == firebaseUser.uid && !localUser.isOffline) {
                        _uiState.value = _uiState.value.copy(
                            user = localUser,
                            isSignedIn = true,
                            isLoading = false
                        )
                        Log.d("DashboardViewModel", "User data loaded and matches Firebase session: ${localUser.name}")
                        loadRecordSummary() // Load summary now that user is confirmed
                    } else {
                        Log.w("DashboardViewModel", "Local user (ID: ${localUser.id}, Offline: ${localUser.isOffline}) mismatch with Firebase session (UID: ${firebaseUser.uid}). Treating as not fully signed in.")
                        // This could happen if local data is stale or sign-out didn't fully clear things.
                        _uiState.value = _uiState.value.copy(
                            user = null, // Or localUser if you want to show some details despite mismatch
                            isSignedIn = false,
                            isLoading = false,
                            errorMessage = if (localUser.id.isNotEmpty() && localUser.id != firebaseUser.uid) context.getString(
                                R.string.account_mismatch
                            ) else null
                        )
                    }
                } else {
                    // No Firebase user, so cannot be signed in.
                    Log.d("DashboardViewModel", "loadUserDataAndDependents: No Firebase user found.")
                    _uiState.value = _uiState.value.copy(
                        user = null,
                        isSignedIn = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading user data", e)
                _uiState.value = _uiState.value.copy(
                    user = null,
                    isSignedIn = false,
                    isLoading = false,
                    errorMessage = context.getString(R.string.failed_to_load_user_data, e.message)
                )
            }
        }
    }

    private suspend fun loadRecordSummary() {
        if (_uiState.value.isSignedIn && _uiState.value.user != null) {
            userRecordSummaryRepository.getCurrentUserSummary()
                .catch { e ->
                    Log.e("DashboardViewModel", "Error loading summary", e)
                    _uiState.value = _uiState.value.copy(errorMessage = context.getString(
                        R.string.failed_to_load_summary,
                        e.message
                    ))
                }
                .collect { summary ->
                    _uiState.value = _uiState.value.copy(recordSummary = summary)
                }
        } else {
            Log.d("DashboardViewModel", "Skipping record summary load: User not signed in or null.")
            _uiState.value = _uiState.value.copy(recordSummary = null) // Clear summary if not signed in
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Helper methods (consider if they are still needed or if UI directly uses uiState.user)
    fun getUserName(): String? = uiState.value.user?.name
    fun getUserEmail(): String? = uiState.value.user?.email
    fun getUserId(): String? = uiState.value.user?.id
    fun isUserSignedIn(): Boolean = uiState.value.isSignedIn && uiState.value.user != null
}