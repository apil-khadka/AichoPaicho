package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.data.repository.RepaymentRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.RecordDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loanRepository: LoanRepository,
    private val repaymentRepository: RepaymentRepository,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    fun loadRecord(loanId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val loan = loanRepository.getLoanById(loanId)
                if (loan != null) {
                    _uiState.value = _uiState.value.copy(loan = loan)

                    // Load contact and type details
                    loan.contactId?.let { contactId ->
                        val contact = contactRepository.getContactById(contactId)
                        _uiState.value = _uiState.value.copy(contact = contact)
                    }

                    val type = typeRepository.getTypeById(loan.typeId)
                    _uiState.value = _uiState.value.copy(type = type)

                    // Observe repayments
                    repaymentRepository.getRepaymentsByLoan(loanId).collectLatest { repayments ->
                        val totalRepaid = repayments.sumOf { it.amount }
                        val remaining = loan.amount - totalRepaid
                        _uiState.value = _uiState.value.copy(
                            repayments = repayments,
                            remainingBalance = remaining,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = context.getString(R.string.record_not_found))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
               // isLoading handled in collect
            }
        }
    }

    fun updateAmount(amount: Double) {
        _uiState.value.loan?.let { loan ->
            val updatedLoan = loan.copy(amount = amount, updatedAt = System.currentTimeMillis())
            val totalRepaid = _uiState.value.repayments.sumOf { it.amount }
            val balance = updatedLoan.amount - totalRepaid
            _uiState.value = _uiState.value.copy(loan = updatedLoan, remainingBalance = balance)

            viewModelScope.launch { loanRepository.update(updatedLoan) }
        }
    }

    fun updateDescription(description: String) {
        _uiState.value.loan?.let { loan ->
             val updatedLoan = loan.copy(
                    description = description,
                    updatedAt = System.currentTimeMillis()
                )
             _uiState.value = _uiState.value.copy(loan = updatedLoan)
             viewModelScope.launch { loanRepository.update(updatedLoan) }
        }
    }

    fun updateDate(date: Long) {
        _uiState.value.loan?.let { loan ->
             val updatedLoan = loan.copy(date = date, updatedAt = System.currentTimeMillis())
             _uiState.value = _uiState.value.copy(loan = updatedLoan)
             viewModelScope.launch { loanRepository.update(updatedLoan) }
        }
    }

    fun addRepayment(amount: Double) {
        val loan = uiState.value.loan ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            try {
                 val repayment = Repayment(
                    id = UUID.randomUUID().toString(),
                    loanId = loan.id,
                    userId = loan.userId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    description = null
                )
                repaymentRepository.insert(repayment)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteRecord() {
        _uiState.value.loan?.let { loan ->
            viewModelScope.launch {
                try {
                    loanRepository.delete(loan.id)
                    _uiState.value = _uiState.value.copy(isRecordDeleted = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun deleteRepayment(repaymentId: String) {
        viewModelScope.launch {
             try {
                repaymentRepository.delete(repaymentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun acknowledgeRecordDeleted()
    {
        _uiState.value = _uiState.value.copy(isRecordDeleted = false)
    }

    fun clearErrorMessage()
    {
        _uiState.value = _uiState.value.copy(errorMessage = null)

    }
}
