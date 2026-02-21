package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record

data class ContactListUiState(
    val contacts: List<Contact> = emptyList(),
    val records: List<Record> = emptyList(),
    val searchQuery: String = "",
    val selectedLetter: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableLetters: List<String> = emptyList()
)