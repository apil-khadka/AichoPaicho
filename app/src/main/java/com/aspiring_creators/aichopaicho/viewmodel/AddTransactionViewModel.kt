package com.aspiring_creators.aichopaicho.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.LoanRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.data.repository.UserRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.AddTransactionUiEvents
import com.aspiring_creators.aichopaicho.viewmodel.data.AddTransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlin.math.roundToLong

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val typeRepository: TypeRepository,
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel(){

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private suspend fun handleSubmit(): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, submissionSuccessful = false) }

        return try {
            if(uiState.value.amount == null){
                throw IllegalArgumentException("Amount cannot be empty")
            }

            val type = typeRepository.getByName(uiState.value.type!!)
            val user = userRepository.getUser()

            val contactInput = uiState.value.contact!!
            // Use existing contact logic or create new
            // Assuming contactInput has at least a name.

            var contactToUse: Contact? = null

            // Try to find existing contact by ID if present
            if (!contactInput.id.isNullOrBlank()) {
                 contactToUse = contactRepository.getContactById(contactInput.id)
            }

            // Or by External Ref
            if (contactToUse == null && !contactInput.externalRef.isNullOrBlank()) {
                contactToUse = contactRepository.getContactByExternalRef(contactInput.externalRef!!)
            }

            // Or by Phone
            val normalizedPhone = contactInput.phone.firstOrNull()?.replace(Regex("[^0-9]"), "")
            if (contactToUse == null && !normalizedPhone.isNullOrBlank()) {
                contactToUse = contactRepository.getContactByPhone(normalizedPhone)
            }

            if (contactToUse == null) {
                // Create new
                contactToUse = Contact(
                    id = UUID.randomUUID().toString(),
                    name = contactInput.name,
                    phone = contactInput.phone,
                    normalizedPhone = normalizedPhone,
                    externalRef = contactInput.externalRef,
                    userId = user.id
                )
                contactRepository.insertContact(contactToUse)
            }

            val loanToSave = Loan(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                typeId = type.id,
                contactId = contactToUse.id,
                amountCents = (uiState.value.amount!! * 100).roundToLong(),
                date = uiState.value.date!!,
                description = uiState.value.description
            )
            loanRepository.insert(loanToSave)

            Log.e("AddTransactionViewModel", "handleSubmit: $loanToSave")

            // Clear form fields after successful save
            _uiState.update { currentState ->
                currentState.copy(
                    submissionSuccessful = true,
                    isLoading = false,
                    // Clear the form fields
                    contact = null,
                    amount = null,
                    description = null
                )
            }

            true // Success
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = e.message,
                    isLoading = false,
                    submissionSuccessful = false
                )
            }
            false // Failure
        }
    }

    fun onEvent(event: AddTransactionUiEvents) {
        when(event) {
            is AddTransactionUiEvents.TypeSelected -> {
                _uiState.value = _uiState.value.copy(
                    type = event.type,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.AmountEntered -> {
                _uiState.value = _uiState.value.copy(
                    amount = event.amount.toDoubleOrNull(),
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.DateEntered -> {
                _uiState.value = _uiState.value.copy(
                    date = event.date,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.DescriptionEntered -> {
                _uiState.value = _uiState.value.copy(
                    description = event.description,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.ContactSelected -> {
                _uiState.value = _uiState.value.copy(
                    contact = event.contact,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.LoadContact -> {
                viewModelScope.launch {
                    try {
                        val contact = contactRepository.getContactById(event.contactId)
                        if (contact != null) {
                            _uiState.value = _uiState.value.copy(contact = contact)
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(errorMessage = e.message)
                    }
                }
            }
            AddTransactionUiEvents.Submit -> {
                viewModelScope.launch {
                    handleSubmit()
                }
            }
        }
    }

    fun clearErrorMessage()
    {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSubmissionSuccessFlag()
    {
        _uiState.update { it.copy(submissionSuccessful = false) }
    }
}
