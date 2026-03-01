package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.UserRecordSummaryRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.viewmodel.data.DashboardScreenUiState
import dev.nyxigale.aichopaicho.viewmodel.data.UpcomingDueItem
import dev.nyxigale.aichopaicho.viewmodel.data.ContactPreview
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userRecordSummaryRepository: UserRecordSummaryRepository,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository,
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
                    _uiState.value = DashboardScreenUiState(isLoading = false, isSignedIn = false, user = null, upcomingDue = emptyList())
                } else {
                    // We have a real local user. Check if it matches a firebase session.
                    val isSignedIn = firebaseUser != null && firebaseUser.uid == localUser.id && !localUser.isOffline
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = isSignedIn,
                        user = localUser
                    )
                    loadUpcomingDueRecords()
                    loadTopContacts()
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

    private suspend fun loadUpcomingDueRecords() {
        try {
            val records = recordRepository.getAllRecords().first()
            val upcoming = records
                .filter { it.dueDate != null && !it.isDeleted && !it.isComplete }
                .sortedBy { it.dueDate }
                .take(3)
                .mapNotNull { record ->
                    val contactName = record.contactId?.let { id ->
                        contactRepository.getContactById(id)?.name
                    } ?: context.getString(R.string.unknown)
                    UpcomingDueItem.from(record, contactName)
                }
            _uiState.value = _uiState.value.copy(upcomingDue = upcoming)
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error loading upcoming due records", e)
        }
    }

    private suspend fun loadTopContacts() {
        try {
            val contacts = contactRepository.getAllContacts().first()
            val records = recordRepository.getAllRecords().first()
            
            val contactSummaries = contacts.map { contact ->
                val contactRecords = records.filter { it.contactId == contact.id && !it.isDeleted }
                val totalLent = contactRecords.filter { it.typeId == dev.nyxigale.aichopaicho.ui.component.TypeConstants.LENT_ID }.sumOf { it.amount.toDouble() }
                val totalBorrowed = contactRecords.filter { it.typeId == dev.nyxigale.aichopaicho.ui.component.TypeConstants.BORROWED_ID }.sumOf { it.amount.toDouble() }
                ContactPreview(contact.id, contact.name, totalLent - totalBorrowed)
            }

            val topLent = contactSummaries.filter { it.amount > 0.0 }.sortedByDescending { it.amount }.take(3)
            val topBorrowed = contactSummaries.filter { it.amount < 0.0 }.sortedBy { it.amount }.take(3)

            _uiState.value = _uiState.value.copy(
                topLentContacts = topLent,
                topBorrowedContacts = topBorrowed
            )
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error loading top contacts", e)
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
