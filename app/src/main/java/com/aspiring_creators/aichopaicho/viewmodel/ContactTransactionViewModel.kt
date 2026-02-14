package com.aspiring_creators.aichopaicho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactTransactionUiState(
    val isLoading: Boolean = false,
    val contact: Contact? = null,
    val loans: List<Loan> = emptyList(),
    val netBalance: Double = 0.0,
    val errorMessage: String? = null
)

@HiltViewModel
class ContactTransactionViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactTransactionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadData(contactId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                contactRepository.getContactById(contactId),
                loanRepository.getLoansWithRepayments(contactId)
            ) { contact, loansWithRepayments ->
                var balance = 0.0

                loansWithRepayments.forEach { item ->
                    val loan = item.loan
                    val repaid = item.repayments.sumOf { it.amount }
                    val remaining = loan.amount - repaid

                    if (loan.typeId == TypeConstants.LENT_ID) {
                        balance += remaining
                    } else {
                        balance -= remaining
                    }
                }

                Triple(contact, loansWithRepayments.map { it.loan }, balance)
            }.collect { (contact, loans, balance) ->
                 _uiState.value = _uiState.value.copy(
                    contact = contact,
                    loans = loans,
                    netBalance = balance,
                    isLoading = false
                )
            }
        }
    }
}
