package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.*
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.TypeRepository
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import dev.nyxigale.aichopaicho.viewmodel.data.ContactTransactionUiState
import dev.nyxigale.aichopaicho.viewmodel.data.TransactionStatusFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale


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
            applyFiltersToCurrentRecords()
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
                _uiState.value = _uiState.value.copy(sourceRecords = recordsWithRepayments)
                applyFiltersToCurrentRecords()
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

    private fun applyFiltersToCurrentRecords() {
        val currentState = _uiState.value
        val filteredRecords = applyFilters(currentState.sourceRecords)
        val (lentRecords, borrowedRecords) = separateRecordsByType(filteredRecords)
        val (totalLent, totalBorrowed) = calculateGrossTotals(filteredRecords)
        val netBalance = calculateNetBalance(filteredRecords)

        _uiState.value = currentState.copy(
            allRecords = filteredRecords,
            lentRecords = lentRecords,
            borrowedRecords = borrowedRecords,
            totalLent = totalLent,
            totalBorrowed = totalBorrowed,
            netBalance = netBalance
        )
    }

    private fun applyFilters(records: List<RecordWithRepayments>): List<RecordWithRepayments> {
        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val normalizedQuery = currentState.searchQuery.trim().lowercase(Locale.getDefault())
        val contact = currentState.contact
        val contactSearchBlob = buildString {
            append(contact?.name.orEmpty())
            append(' ')
            append(contact?.phone?.filterNotNull()?.joinToString(" ").orEmpty())
        }.lowercase(Locale.getDefault())

        return records.filter { recordWithRepayments ->
            val record = recordWithRepayments.record

            when (currentState.statusFilter) {
                TransactionStatusFilter.OPEN -> {
                    if (recordWithRepayments.isSettled) return@filter false
                }

                TransactionStatusFilter.COMPLETED -> {
                    if (!recordWithRepayments.isSettled) return@filter false
                }

                TransactionStatusFilter.OVERDUE -> {
                    val dueDate = record.dueDate
                    if (dueDate == null || dueDate >= now || recordWithRepayments.isSettled) {
                        return@filter false
                    }
                }

                TransactionStatusFilter.ALL -> Unit
            }

            if (normalizedQuery.isNotBlank()) {
                val searchable = buildString {
                    append(contactSearchBlob)
                    append(' ')
                    append(record.description.orEmpty())
                    append(' ')
                    append(record.amount.toString())
                    append(' ')
                    append(
                        when (record.typeId) {
                            TypeConstants.LENT_ID -> "lent"
                            TypeConstants.BORROWED_ID -> "borrowed"
                            else -> ""
                        }
                    )
                }.lowercase(Locale.getDefault())

                if (!searchable.contains(normalizedQuery)) {
                    return@filter false
                }
            }
            true
        }
    }

    fun updateShowCompleted(showCompleted: Boolean) {
        _uiState.value = _uiState.value.copy(
            showCompleted = showCompleted,
            statusFilter = if (showCompleted) {
                TransactionStatusFilter.ALL
            } else {
                TransactionStatusFilter.OPEN
            }
        )
        applyFiltersToCurrentRecords()
    }

    fun updateStatusFilter(statusFilter: TransactionStatusFilter) {
        _uiState.value = _uiState.value.copy(
            statusFilter = statusFilter,
            showCompleted = statusFilter != TransactionStatusFilter.OPEN
        )
        applyFiltersToCurrentRecords()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersToCurrentRecords()
    }

    fun updateSelectedTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
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

    fun restoreDeletedRecord(record: Record) {
        viewModelScope.launch {
            try {
                recordRepository.updateRecord(
                    record.copy(
                        isDeleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
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
