package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.*
import com.aspiring_creators.aichopaicho.data.repository.*
import com.aspiring_creators.aichopaicho.viewmodel.data.ViewTransactionViewModelUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.get

@HiltViewModel
class ViewTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val userRecordSummaryRepository: UserRecordSummaryRepository,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewTransactionViewModelUiState())
    val uiState: StateFlow<ViewTransactionViewModelUiState> = _uiState.asStateFlow()


    init {
        _uiState.value = _uiState.value.copy(
            dateRange = Long.MIN_VALUE to Long.MAX_VALUE
        )
    }

    fun loadInitialData() {
        viewModelScope.launch {
            setLoading(true)
            try {
                launch { loadContacts() }
                launch { loadRecords() }
                launch { loadTypes() }
            } catch (e: Exception) {
                setErrorMessage(e.message ?: context.getString(R.string.unknown_error_occurred))
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun loadRecords() {
        val (startDate, endDate) = _uiState.value.dateRange
        recordRepository.getRecordsByDateRange(startDate, endDate)
            .catch { e -> setErrorMessage(context.getString(
                R.string.failed_to_load_records,
                e.message
            )) }
            .collect { records ->
                _uiState.value = _uiState.value.copy(
                    records = records,
                    filteredRecords = applyFilters(records),
                )
            }
    }


    private suspend fun loadContacts() {
        contactRepository.getAllContacts()
            .catch { e -> setErrorMessage(context.getString(
                R.string.failed_to_load_contacts,
                e.message
            )) }
            .collect { contacts ->
                val contactMap = contacts.associateBy { it.id }
                _uiState.value = _uiState.value.copy(contacts = contactMap)
            }
    }

    private suspend fun loadTypes() {
        typeRepository.getAllTypes()
            .catch { e -> setErrorMessage(context.getString(
                R.string.failed_to_load_types,
                e.message
            )) }
            .collect { types ->
                val typeMap = types.associateBy { it.id }
                _uiState.value = _uiState.value.copy(types = typeMap)
            }
    }

    fun updateDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(dateRange = startDate to endDate)
        viewModelScope.launch {
            loadRecords()
        }
    }

    fun updateSelectedType(typeId: Int?) {
        _uiState.value = _uiState.value.copy(selectedType = typeId)
        applyFiltersToCurrentRecords()
    }

    fun updateFromQuery(query: String) {
        _uiState.value = _uiState.value.copy(fromQuery = query)
    }

    fun updateMoneyToQuery(query: String) {
        _uiState.value = _uiState.value.copy(moneyToQuery = query)
    }

    fun updateMoneyFilterApplyClicked() {
        applyFiltersToCurrentRecords()
    }

    fun updateShowCompleted(showCompleted: Boolean) {
        _uiState.value = _uiState.value.copy(showCompleted = showCompleted)
        applyFiltersToCurrentRecords()
    }

    private fun applyFiltersToCurrentRecords() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            filteredRecords = applyFilters(currentState.records)
        )
    }

    private fun applyFilters(records: List<RecordWithRepayments>): List<RecordWithRepayments> {
        val currentState = _uiState.value
        return records.filter { recordWithRepayments ->
            // Filter by completion status
            if (!currentState.showCompleted && recordWithRepayments.isSettled) return@filter false

            // Filter by type
            currentState.selectedType?.let { typeId ->
                if (recordWithRepayments.record.typeId != typeId) return@filter false
            }

            // Filter by amount range
            if (currentState.fromQuery.isNotBlank() && currentState.moneyToQuery.isNotBlank()) {
                val amount = recordWithRepayments.record.amount
                if ( !(amount >= currentState.fromQuery.toInt() && amount <= currentState.moneyToQuery.toInt())) {
                    return@filter false
                }
            }
            true
        }
    }

    // This is now obsolete as completion is derived
    // fun toggleRecordCompletion(recordId: String) { ... }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            try {
                recordRepository.deleteRecord(recordId)
            } catch (e: Exception) {
                setErrorMessage(context.getString(
                    R.string.failed_to_delete_record,
                    e.message
                ))
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
                setErrorMessage(context.getString(
                    R.string.failed_to_update_record,
                    e.message
                ))
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
