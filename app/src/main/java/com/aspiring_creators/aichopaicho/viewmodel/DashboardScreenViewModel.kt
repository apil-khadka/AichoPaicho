package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRecordSummaryRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.DashboardScreenUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardScreenUiState(isLoading = true))
    val uiState: StateFlow<DashboardScreenUiState> = _uiState.asStateFlow()

    private var summaryJob: Job? = null
    private var loansJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                Log.d("DashboardViewModel", "AuthStateListener: Firebase user signed in (UID: ${firebaseUser.uid}). Loading data.")
                loadUserDataAndDependents()
            } else {
                Log.d("DashboardViewModel", "AuthStateListener: Firebase user signed out. Clearing UI state.")
                _uiState.value = DashboardScreenUiState(isSignedIn = false)
                cancelJobs()
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private fun cancelJobs() {
        summaryJob?.cancel()
        loansJob?.cancel()
    }

    fun loadUserDataAndDependents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val localUser = userRepository.getUser()

                    if (localUser.id == firebaseUser.uid) {
                        _uiState.value = _uiState.value.copy(
                            user = localUser,
                            isSignedIn = true,
                            isLoading = false
                        )
                        loadRecordSummary()
                        loadRecentLoans()
                    } else {
                        Log.w("DashboardViewModel", "Local user ID ${localUser.id} mismatch with Firebase ${firebaseUser.uid}")
                        cancelJobs()
                        _uiState.value = DashboardScreenUiState(
                            user = null,
                            isSignedIn = false,
                            isLoading = false,
                            errorMessage = context.getString(R.string.account_mismatch),
                            recordSummary = null,
                            recentLoans = emptyList()
                        )
                    }
                } else {
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

    private fun loadRecordSummary() {
        summaryJob?.cancel()
        summaryJob = viewModelScope.launch {
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
        }
    }

    private fun loadRecentLoans() {
        loansJob?.cancel()
        loansJob = viewModelScope.launch {
            loanRepository.getRecentLoans(5)
                .catch { e ->
                    Log.e("DashboardViewModel", "Error loading recent loans", e)
                }
                .collect { loans ->
                    _uiState.value = _uiState.value.copy(recentLoans = loans)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
