package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactTransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.aspiring_creators.aichopaicho.data.entity.*
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import dagger.hilt.android.qualifiers.ApplicationContext


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
                launch { loadRecords(contactId) }
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

    private suspend fun loadRecords(contactId: String) {
        recordRepository.getRecordsByContactId(contactId)
            .catch { e -> setErrorMessage(
                context.getString(
                    R.string.failed_to_load_records,
                    e.message
                ))
            }
            .collect { records ->
                val filteredRecords = if (_uiState.value.showCompleted) {
                    records
                } else {
                    records.filter { !it.isComplete }
                }

                val (lentRecords, borrowedRecords) = separateRecordsByType(filteredRecords)
                val (totalLent, totalBorrowed) = calculateTotals(filteredRecords)

                _uiState.value = _uiState.value.copy(
                    allRecords = filteredRecords,
                    lentRecords = lentRecords,
                    borrowedRecords = borrowedRecords,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    netBalance = totalLent - totalBorrowed
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

    private fun separateRecordsByType(records: List<Record>): Pair<List<Record>, List<Record>> {
        val lent = records.filter { it.typeId == TypeConstants.LENT_ID}.sortedByDescending { it.date }
        val borrowed = records.filter { it.typeId == TypeConstants.BORROWED_ID }.sortedByDescending { it.date }
        return lent to borrowed
    }

    private fun calculateTotals(records: List<Record>): Pair<Double, Double> {
        val totalLent = records.filter { it.typeId == TypeConstants.LENT_ID }.sumOf { it.amount.toDouble() }
        val totalBorrowed = records.filter { it.typeId == TypeConstants.BORROWED_ID }.sumOf { it.amount.toDouble() }
        return totalLent to totalBorrowed
    }

    fun updateShowCompleted(showCompleted: Boolean) {
        _uiState.value = _uiState.value.copy(showCompleted = showCompleted)
        // Reload records with new filter
        _uiState.value.contact?.id?.let { contactId ->
            viewModelScope.launch {
                loadRecords(contactId)
            }
        }
    }

    fun updateSelectedTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }

    fun toggleRecordCompletion(recordId: String) {
        viewModelScope.launch {
            try {
                val record = _uiState.value.allRecords.find { it.id == recordId }
                record?.let {
                    val updatedRecord = it.copy(
                        isComplete = !it.isComplete,
                        updatedAt = System.currentTimeMillis()
                    )
                    recordRepository.updateRecord(updatedRecord)
                }
            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_update_record, e.message))
            }
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            try {
                recordRepository.deleteRecord(recordId)
            } catch (e: Exception) {
                setErrorMessage(context.getString(R.string.failed_to_delete_record, e.message))
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