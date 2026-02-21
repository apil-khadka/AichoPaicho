package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.User
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

    private val authStateListener = FirebaseAuth.AuthStateListener {
        // Auth state changed, re-evaluate everything.
        refresh()
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        // The listener will also be called immediately with the current auth state,
        // which triggers the initial data load.
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val localUser = userRepository.getUser() // Returns non-null User, possibly sentinel
                val firebaseUser = firebaseAuth.currentUser

                // Check for the sentinel user, which indicates no actual user in the DB
                if (localUser.id.isEmpty()) {
                    _uiState.value = DashboardScreenUiState(isLoading = false, isSignedIn = false, user = null)
                } else {
                    // We have a real local user. Check if it matches a firebase session.
                    val isSignedIn = firebaseUser != null && firebaseUser.uid == localUser.id && !localUser.isOffline
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = isSignedIn,
                        user = localUser
                    )
                    if (isSignedIn) {
                        loadRecordSummary()
                    } else {
                        // It's an offline user, clear the summary.
                        _uiState.value = _uiState.value.copy(recordSummary = null)
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error refreshing UI state", e)
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
