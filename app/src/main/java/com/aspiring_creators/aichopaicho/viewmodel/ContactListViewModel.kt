package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.data.repository.RepaymentRepository
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val loanRepository: LoanRepository,
    private val repaymentRepository: RepaymentRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                contactRepository.getAllContacts(),
                loanRepository.getAllLoans(),
                repaymentRepository.getAllRepayments()
            ) { contacts, loans, repayments ->
                val sortedContacts = contacts.sortedBy { it.name.uppercase() }

                val balances = sortedContacts.associate { contact ->
                    val contactLoans = loans.filter { it.contactId == contact.id }
                    val repaymentMap = repayments.groupBy { it.loanId }

                    var balance = 0.0
                    contactLoans.forEach { loan ->
                        val loanRepayments = repaymentMap[loan.id] ?: emptyList()
                        val repaid = loanRepayments.sumOf { it.amount }
                        val remaining = loan.amount - repaid

                        if (loan.typeId == TypeConstants.LENT_ID) {
                            balance += remaining
                        } else {
                            balance -= remaining
                        }
                    }
                    contact.id to balance
                }
                Triple(sortedContacts, loans, balances)
            }
            .catch { e ->
                setErrorMessage(context.getString(R.string.failed_to_load_contacts, e.message))
            }
            .collect { (contacts, loans, balances) ->
                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    loans = loans,
                    contactBalances = balances,
                    isLoading = false
                )
            }
        }
    }


    fun searchContacts(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun jumpToLetter(letter: String) {
        _uiState.value = _uiState.value.copy(selectedLetter = letter)
    }

    fun getFilteredContacts(type: String): List<Contact> {
        val searchQuery = _uiState.value.searchQuery.trim()
        val selectedLetter = _uiState.value.selectedLetter
        val contacts = _uiState.value.contacts
        val loans = _uiState.value.loans

        val targetTypeId = if (type == TypeConstants.TYPE_LENT) TypeConstants.LENT_ID
                           else if (type == TypeConstants.TYPE_BORROWED) TypeConstants.BORROWED_ID
                           else null

        var filteredContacts = if (targetTypeId != null) {
            contacts.filter { contact ->
                loans.any { loan ->
                    loan.typeId == targetTypeId && loan.contactId == contact.id
                }
            }
        } else {
            contacts
        }

        if (searchQuery.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedLetter.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.uppercase().startsWith(selectedLetter.uppercase())
            }
        }

        return filteredContacts
    }

    fun getAvailableLettersForCurrentState(type: String): List<String> {
         val searchQuery = _uiState.value.searchQuery.trim()
         val contacts = _uiState.value.contacts
         val loans = _uiState.value.loans

        val targetTypeId = if (type == TypeConstants.TYPE_LENT) TypeConstants.LENT_ID
                           else if (type == TypeConstants.TYPE_BORROWED) TypeConstants.BORROWED_ID
                           else null

        var filteredContacts = if (targetTypeId != null) {
            contacts.filter { contact ->
                loans.any { loan ->
                    loan.typeId == targetTypeId && loan.contactId == contact.id
                }
            }
        } else {
            contacts
        }

        if (searchQuery.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }

        return filteredContacts
            .map { it.name.firstOrNull()?.uppercase()?.toString() ?: "" }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", selectedLetter = "")
    }

    private fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }
    fun clearErrorMessage()
    {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}
