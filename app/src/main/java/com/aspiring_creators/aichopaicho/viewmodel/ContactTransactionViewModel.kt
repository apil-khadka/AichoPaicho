package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.*
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactTransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


@HiltViewModel
class ContactTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = ContactTransactionUiState())
    val uiState: StateFlow<ContactTransactionUiState> = _uiState.asStateFlow()

    fun loadContactRecords(contactId: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                // Load all data concurrently
                launch { loadContact(contactId) }
                launch { loadRecordsAndCalculations(contactId) } // Renamed and refactored
                launch { loadTypes() }
            } catch (e: Exception) {
                setErrorMessage(
                    context.getString(
                        R.string.failed_to_load_contact_records,
                        e.message
                    ))
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun loadContact(contactId: String) {
        try {
            val contact = contactRepository.getContactById(contactId)
            _uiState.value = _uiState.value.copy(contact = contact)
        } catch (e: Exception) {
            setErrorMessage(
                context.getString(
                    R.string.failed_to_load_contacts,
                    e.message
                ))
        }
    }

    private suspend fun loadRecordsAndCalculations(contactId: String) {
        // Use the new repository method to get records with their repayments
        recordRepository.getRecordsWithRepaymentsByContactId(contactId)
            .catch { e -> setErrorMessage(
                context.getString(
                    R.string.failed_to_load_records,
                    e.message
                ))
            }
            .collect { recordsWithRepayments ->

                // Filter settled records based on the new isSettled property
                val filteredRecords = if (_uiState.value.showCompleted) {
                    recordsWithRepayments
                } else {
                    recordsWithRepayments.filter { !it.isSettled }
                }

                val (lentRecords, borrowedRecords) = separateRecordsByType(filteredRecords)
                val (totalLent, totalBorrowed) = calculateGrossTotals(filteredRecords)
                val netBalance = calculateNetBalance(filteredRecords)

                _uiState.value = _uiState.value.copy(
                    allRecords = filteredRecords,
                    lentRecords = lentRecords,
                    borrowedRecords = borrowedRecords,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    netBalance = netBalance
                )
            }
    }

    private suspend fun loadTypes() {
        typeRepository.getAllTypes()
            .catch { e -> setErrorMessage(
                context.getString(
                    R.string.failed_to_load_types,
                    e.message
                )) }
            .collect { types ->
                val typeMap = types.associateBy { it.id }
                _uiState.value = _uiState.value.copy(types = typeMap)
            }
    }

    private fun separateRecordsByType(records: List<RecordWithRepayments>): Pair<List<RecordWithRepayments>, List<RecordWithRepayments>> {
        val lent = records.filter { it.record.typeId == TypeConstants.LENT_ID}.sortedByDescending { it.record.date }
        val borrowed = records.filter { it.record.typeId == TypeConstants.BORROWED_ID }.sortedByDescending { it.record.date }
        return lent to borrowed
    }

    private fun calculateGrossTotals(records: List<RecordWithRepayments>): Pair<Double, Double> {
        val totalLent = records.filter { it.record.typeId == TypeConstants.LENT_ID }.sumOf { it.record.amount.toDouble() }
        val totalBorrowed = records.filter { it.record.typeId == TypeConstants.BORROWED_ID }.sumOf { it.record.amount.toDouble() }
        return totalLent to totalBorrowed
    }

    private fun calculateNetBalance(records: List<RecordWithRepayments>): Double {
        val outstandingLent = records.filter { it.record.typeId == TypeConstants.LENT_ID }.sumOf { it.remainingAmount.toDouble() }
        val outstandingBorrowed = records.filter { it.record.typeId == TypeConstants.BORROWED_ID }.sumOf { it.remainingAmount.toDouble() }
        return outstandingLent - outstandingBorrowed
    }

    fun updateShowCompleted(showCompleted: Boolean) {
        _uiState.value = _uiState.value.copy(showCompleted = showCompleted)
        // Reload records with new filter
        _uiState.value.contact?.id?.let { contactId ->
            viewModelScope.launch {
                loadRecordsAndCalculations(contactId)
            }
        }
    }

    fun updateSelectedTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    // This function is now obsolete, as completion is derived from repayments.
    // Manual toggling is no longer supported in this ViewModel.
    // fun toggleRecordCompletion(recordId: String) { ... }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            try {
                recordRepository.deleteRecord(recordId)
            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_delete_record, e.message))
            }
        }
    }

    fun toggleRecordCompletion(recordId: String, isComplete: Boolean) {
        viewModelScope.launch {
            try {
                val record = recordRepository.getRecordById(recordId) ?: return@launch
                recordRepository.updateRecord(
                    record.copy(isComplete = isComplete, updatedAt = System.currentTimeMillis())
                )
            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_update_record, e.message))
            }
        }
    }

    fun setErrorMessage(value: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = value)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = value)
    }
}
