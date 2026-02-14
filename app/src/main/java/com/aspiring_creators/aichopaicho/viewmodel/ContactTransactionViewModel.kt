package com.aspiring_creators.aichopaicho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.LoanWithRepayments
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

            try {
                val contact = contactRepository.getContactById(contactId)

                loanRepository.getLoansWithRepayments(contactId).collect { loansWithRepayments ->
                    var balanceCents = 0L

                    loansWithRepayments.forEach { item ->
                        val loan = item.loan
                        val repaidCents = item.repayments.sumOf { it.amountCents }
                        val remainingCents = loan.amountCents - repaidCents

                        if (loan.typeId == TypeConstants.LENT_ID) {
                            balanceCents += remainingCents
                        } else {
                            balanceCents -= remainingCents
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        contact = contact,
                        loans = loansWithRepayments.map { it.loan },
                        netBalance = balanceCents / 100.0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
