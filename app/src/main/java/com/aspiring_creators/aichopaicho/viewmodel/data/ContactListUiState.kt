package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.Repayment

data class ContactListUiState(
    val contacts: List<Contact> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val repayments: List<Repayment> = emptyList(),
    val contactBalances: Map<String, Double> = emptyMap(),
    val searchQuery: String = "",
    val selectedLetter: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableLetters: List<String> = emptyList()
)