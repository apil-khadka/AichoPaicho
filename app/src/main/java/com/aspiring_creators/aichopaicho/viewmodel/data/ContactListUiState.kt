package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Record

data class ContactListUiState(
    val contacts: List<Contact> = emptyList(),
    val records: List<Record> = emptyList(),
    val searchQuery: String = "",
    val selectedLetter: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableLetters: List<String> = emptyList()
)