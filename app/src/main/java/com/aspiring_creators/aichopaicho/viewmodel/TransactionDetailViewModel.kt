package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.data.repository.RepaymentRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.RecordDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository,
    private val repaymentRepository: RepaymentRepository // Added repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                recordRepository.getRecordWithRepaymentsById(recordId)
                    .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
                    .collect { recordWithRepayments ->
                        if (recordWithRepayments != null) {
                            _uiState.value = _uiState.value.copy(recordWithRepayments = recordWithRepayments)

                            // Load contact and type details
                            recordWithRepayments.record.contactId?.let { contactId ->
                                val contact = contactRepository.getContactById(contactId)
                                _uiState.value = _uiState.value.copy(contact = contact)
                            }
                            val type = typeRepository.getTypeById(recordWithRepayments.record.typeId)
                            _uiState.value = _uiState.value.copy(type = type, isLoading = false)
                        } else {
                            _uiState.value = _uiState.value.copy(errorMessage = context.getString(R.string.record_not_found), isLoading = false)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    // --- Repayment Logic ---

    fun onRepaymentAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(repaymentAmount = amount)
    }

    fun onRepaymentDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(repaymentDescription = description)
    }

    fun saveRepayment() {
        viewModelScope.launch {
            val recordId = _uiState.value.recordWithRepayments?.record?.id
            val amount = _uiState.value.repaymentAmount.toIntOrNull()

            if (recordId == null || amount == null || amount <= 0) {
                _uiState.value = _uiState.value.copy(errorMessage = "Invalid amount")
                return@launch
            }

            val newRepayment = Repayment(
                recordId = recordId,
                amount = amount,
                date = System.currentTimeMillis(),
                description = _uiState.value.repaymentDescription.takeIf { it.isNotBlank() }
            )

            try {
                repaymentRepository.insertRepayment(newRepayment)
                // Clear form and set flag
                _uiState.value = _uiState.value.copy(
                    repaymentAmount = "",
                    repaymentDescription = "",
                    repaymentSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save repayment: ${e.message}")
            }
        }
    }

    fun acknowledgeRepaymentSaved() {
        _uiState.value = _uiState.value.copy(repaymentSaved = false)
    }


    // --- Existing Edit/Delete Logic (Adjusted) ---

    fun updateAmount(amountString: String) {
        val amount = amountString.toIntOrNull() ?: 0
        _uiState.value.recordWithRepayments?.let {
            _uiState.value = _uiState.value.copy(
                recordWithRepayments = it.copy(record = it.record.copy(amount = amount, updatedAt = System.currentTimeMillis()))
            )
        }
    }

    fun updateDescription(description: String) {
        _uiState.value.recordWithRepayments?.let {
            _uiState.value = _uiState.value.copy(
                recordWithRepayments = it.copy(record = it.record.copy(description = description, updatedAt = System.currentTimeMillis()))
            )
        }
    }

    fun updateDate(date: Long) {
        _uiState.value.recordWithRepayments?.let {
            _uiState.value = _uiState.value.copy(
                recordWithRepayments = it.copy(record = it.record.copy(date = date, updatedAt = System.currentTimeMillis()))
            )
        }
    }

    fun updateDueDate(dueDate: Long?) {
        _uiState.value.recordWithRepayments?.let {
            _uiState.value = _uiState.value.copy(
                recordWithRepayments = it.copy(record = it.record.copy(dueDate = dueDate, updatedAt = System.currentTimeMillis()))
            )
        }
    }

    fun saveRecord() {
        _uiState.value.recordWithRepayments?.record?.let { record ->
            viewModelScope.launch {
                try {
                    recordRepository.updateRecord(record)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun deleteRecord() {
        _uiState.value.recordWithRepayments?.record?.let { record ->
            viewModelScope.launch {
                try {
                    recordRepository.deleteRecord(record.id)
                    _uiState.value = _uiState.value.copy(isRecordDeleted = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun toggleRecordCompletion(isComplete: Boolean) {
        _uiState.value.recordWithRepayments?.record?.let { record ->
            viewModelScope.launch {
                try {
                    val updated = record.copy(isComplete = isComplete, updatedAt = System.currentTimeMillis())
                    recordRepository.updateRecord(updated)
                    _uiState.value = _uiState.value.copy(
                        recordWithRepayments = _uiState.value.recordWithRepayments?.copy(record = updated)
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun acknowledgeRecordDeleted() {
        _uiState.value = _uiState.value.copy(isRecordDeleted = false)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
