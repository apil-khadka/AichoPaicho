package dev.nyxigale.aichopaicho.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.TypeRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiEvents
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.auth.FirebaseAuth
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val typeRepository: TypeRepository,
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel(){

    private val _uiState = MutableStateFlow(AddTransactionUiState(type = dev.nyxigale.aichopaicho.ui.component.TypeConstants.TYPE_LENT))
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        loadRecentContacts()
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        loadRecentContacts()
    }

    private fun loadRecentContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts()
                .catch { e -> Log.e("AddTxnViewModel", "Error loading recent contacts", e) }
                .collect { contacts ->
                    _uiState.update { it.copy(recentContacts = contacts.take(5)) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private suspend fun handleSubmit(): Boolean {
        val state = uiState.value
        val validationErrors = validate(state)
        if (validationErrors.hasAnyError()) {
            _uiState.update {
                it.copy(
                    contactNameError = validationErrors.contactNameError,
                    contactPhoneError = validationErrors.contactPhoneError,
                    amountError = validationErrors.amountError,
                    dateError = validationErrors.dateError,
                    errorMessage = validationErrors.firstError(),
                    isLoading = false,
                    submissionSuccessful = false
                )
            }
            return false
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                submissionSuccessful = false,
                contactNameError = null,
                contactPhoneError = null,
                amountError = null,
                dateError = null
            )
        }

        return try {
            val currentState = uiState.value
            val parsedAmount = currentState.amountInput.toIntOrNull() ?: 0

            val type = typeRepository.getByName(currentState.type!!)
                ?: throw IllegalArgumentException(context.getString(R.string.error_transaction_type_not_found))
            val user = userRepository.getUser()

            val selectedContactInfo = resolveContactInput(currentState)
                ?: throw IllegalArgumentException(context.getString(R.string.error_select_contact))
            val primaryPhoneNumber = selectedContactInfo.phone
                .firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(context.getString(R.string.error_enter_contact_number))
            if (!isPhoneNumberValid(primaryPhoneNumber)) {
                throw IllegalArgumentException(context.getString(R.string.error_enter_valid_contact_number))
            }

            var canonicalContact: Contact? = null
            canonicalContact = contactRepository.getContactByPhoneNumber(primaryPhoneNumber)
            
            val contactToSave: Contact
            if (canonicalContact == null) {
                val phonesToSave = selectedContactInfo.phone
                    .mapNotNull { it?.trim() }
                    .filter { isPhoneNumberValid(it) }
                    .ifEmpty { listOf(primaryPhoneNumber) }
                // No canonical contact exists, create a new one owned by the current user.
                contactToSave = Contact(
                    id = UUID.randomUUID().toString(),
                    name = selectedContactInfo.name.trim(),
                    phone = phonesToSave,
                    contactId = selectedContactInfo.contactId, // Can still save this for reference
                    userId = user.id // The current user becomes the owner/creator
                )
                contactRepository.insertContact(contactToSave) // Use the simple insert
            } else {
                contactToSave = canonicalContact
            }

            val recordToSave = Record(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                typeId = type.id,
                contactId = contactToSave.id,
                amount = parsedAmount,
                date = currentState.date!!,
                dueDate = currentState.dueDate,
                description = currentState.description
            )
            recordRepository.upsert(recordToSave)

            Log.e("AddTransactionViewModel", "handleSubmit: $recordToSave")

            // Clear form fields after successful save
            _uiState.update { currentState ->
                currentState.copy(
                    submissionSuccessful = true,
                    isLoading = false,
                    // Clear the form fields
                    contact = null,
                    contactNameInput = "",
                    contactPhoneInput = "",
                    amountInput = "",
                    amount = null,
                    date = null,
                    description = null,
                    dueDate = null,
                    contactNameError = null,
                    contactPhoneError = null,
                    amountError = null,
                    dateError = null,
                    type = dev.nyxigale.aichopaicho.ui.component.TypeConstants.TYPE_LENT // Reset to default
                )
            }

            true // Success
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = e.message ?: context.getString(R.string.unknown_error_occurred),
                    contactNameError = null,
                    contactPhoneError = null,
                    amountError = null,
                    dateError = null,
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
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.ContactNameEntered -> {
                _uiState.value = _uiState.value.copy(
                    contact = null,
                    contactNameInput = event.name,
                    submissionSuccessful = false,
                    errorMessage = null,
                    contactNameError = null
                )
            }
            is AddTransactionUiEvents.ContactPhoneEntered -> {
                _uiState.value = _uiState.value.copy(
                    contact = null,
                    contactPhoneInput = event.phone,
                    submissionSuccessful = false,
                    errorMessage = null,
                    contactPhoneError = null
                )
            }
            is AddTransactionUiEvents.AmountEntered -> {
                _uiState.value = _uiState.value.copy(
                    amountInput = event.amount,
                    amount = event.amount.toIntOrNull(),
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null,
                    amountError = null
                )
            }
            is AddTransactionUiEvents.DateEntered -> {
                _uiState.value = _uiState.value.copy(
                    date = event.date,
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null,
                    dateError = null
                )
            }
            is AddTransactionUiEvents.DueDateEntered -> {
                _uiState.value = _uiState.value.copy(
                    dueDate = event.dueDate,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.DescriptionEntered -> {
                _uiState.value = _uiState.value.copy(
                    description = event.description,
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.ContactSelected -> {
                val safeContact = sanitizeContact(event.contact)
                _uiState.value = _uiState.value.copy(
                    contact = safeContact,
                    contactNameInput = safeContact?.name.orEmpty(),
                    contactPhoneInput = safeContact?.phone?.firstOrNull().orEmpty(),
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null,
                    contactNameError = null,
                    contactPhoneError = null
                )
            }
            AddTransactionUiEvents.Submit -> {
                viewModelScope.launch {
                    handleSubmit()
                    // No need to handle snackbar here, let the screen handle it reactively
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

    private fun validate(state: AddTransactionUiState): ValidationErrors {
        val usingPickedContact = isContactValid(state.contact)
        val contactNameError = if (!usingPickedContact && state.contactNameInput.isBlank()) {
            context.getString(R.string.error_enter_contact_name)
        } else {
            null
        }
        val contactPhoneError = null // Phone is optional now

        val amountError = when {
            state.amountInput.isBlank() -> context.getString(R.string.error_enter_amount)
            state.amountInput.toIntOrNull()?.let { it > 0 } != true ->
                context.getString(R.string.error_enter_valid_amount)
            else -> null
        }

        val dateError = if (state.date == null) {
            context.getString(R.string.error_select_date)
        } else {
            null
        }

        return ValidationErrors(
            contactNameError = contactNameError,
            contactPhoneError = contactPhoneError,
            amountError = amountError,
            dateError = dateError
        )
    }

    private fun resolveContactInput(state: AddTransactionUiState): Contact? {
        val picked = sanitizeContact(state.contact)
        if (picked != null) return picked

        val manualName = state.contactNameInput.trim()
        val manualPhone = state.contactPhoneInput.trim()
        if (manualName.isBlank()) return null

        return Contact(
            id = "",
            name = manualName,
            userId = null,
            phone = if (manualPhone.isBlank()) emptyList() else listOf(manualPhone),
            contactId = null
        )
    }

    private fun sanitizeContact(contact: Contact?): Contact? {
        return if (isContactValid(contact)) contact else null
    }

    private fun isContactValid(contact: Contact?): Boolean {
        return contact != null &&
            contact.name.isNotBlank() &&
            (contact.phone.isEmpty() || contact.phone.any { isPhoneNumberValid(it) })
    }

    private fun isPhoneNumberValid(phone: String?): Boolean {
        val value = phone?.trim().orEmpty()
        if (value.isBlank()) return true // Optional
        if (value.any { !it.isDigit() && it !in setOf('+', '-', ' ', '(', ')') }) return false
        val digitCount = value.count(Char::isDigit)
        return digitCount >= 5
    }

    private data class ValidationErrors(
        val contactNameError: String? = null,
        val contactPhoneError: String? = null,
        val amountError: String? = null,
        val dateError: String? = null
    ) {
        fun hasAnyError(): Boolean {
            return contactNameError != null ||
                contactPhoneError != null ||
                amountError != null ||
                dateError != null
        }

        fun firstError(): String? {
            return contactNameError ?: contactPhoneError ?: amountError ?: dateError
        }
    }

}
