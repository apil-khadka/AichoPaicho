package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val recordRepository: RecordRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadContacts()
        }
        viewModelScope.launch {
            loadRecords()
        }
    }

    private suspend fun loadContacts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        contactRepository.getAllContacts()
            .catch { e -> setErrorMessage(
                context.getString(
                    R.string.failed_to_load_contacts,
                    e.message
                )) }
            .collect { contacts ->
                val sortedContacts = contacts.sortedBy { it.name.uppercase() }
                _uiState.value = _uiState.value.copy(
                    contacts = sortedContacts,
                    isLoading = false
                )
            }
    }

    private suspend fun loadRecords() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        recordRepository.getAllRecords()
            .catch { e -> setErrorMessage(
                context.getString(
                    R.string.failed_to_load_records,
                    e.message
                )
            ) }
            .collect { records ->
                _uiState.value = _uiState.value.copy(
                    records = records,
                    isLoading = false
                )
            }
    }


    fun searchContacts(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun jumpToLetter(letter: String) {
        _uiState.value = _uiState.value.copy(selectedLetter = letter)
    }

    fun getContactsByType(type: String): List<Contact> {
        val contacts = _uiState.value.contacts
        if(type.isBlank())
        {
            return contacts
        }
        val records = _uiState.value.records
        val targetTypeId = if (type == TypeConstants.TYPE_LENT) TypeConstants.LENT_ID else TypeConstants.BORROWED_ID

        // Filter the list of contacts
        val contactsByType: List<Contact> = contacts.filter { contact ->
            records.any { record ->
                record.typeId == targetTypeId && record.contactId == contact.id
            }
        }
        return contactsByType
    }

    fun getFilteredContacts(type: String): List<Contact> {
        val searchQuery = _uiState.value.searchQuery.trim()
        val selectedLetter = _uiState.value.selectedLetter

        var filteredContacts = getContactsByType(type)

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply letter filter
        if (selectedLetter.isNotBlank()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.name.uppercase().startsWith(selectedLetter.uppercase())
            }
        }
       _uiState.value = _uiState.value.copy( availableLetters = getAvailableLetters(filteredContacts))
        return filteredContacts
    }

    fun getAvailableLetters(contacts:List<Contact>): List<String> {
        return contacts
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