package dev.nyxigale.aichopaicho.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecurringTemplate
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecurringTemplateRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.TypeRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiEvents
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiState
import dev.nyxigale.aichopaicho.viewmodel.data.RecurrenceType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val typeRepository: TypeRepository,
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
    private val recurringTemplateRepository: RecurringTemplateRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private suspend fun handleSubmit(): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, submissionSuccessful = false) }

        return try {
            val state = uiState.value
            val validationError = validate(state)
            if (validationError != null) {
                throw IllegalArgumentException(validationError)
            }

            val parsedAmount = state.amountInput.toIntOrNull() ?: 0

            val type = typeRepository.getByName(state.type!!)
                ?: throw IllegalArgumentException(context.getString(R.string.error_transaction_type_not_found))
            val user = userRepository.getUser()

            val selectedContactInfo = state.contact!!
            val primaryPhoneNumber = selectedContactInfo.phone.firstOrNull()

            var canonicalContact: Contact? = null
            if (primaryPhoneNumber != null) {
                canonicalContact = contactRepository.getContactByPhoneNumber(primaryPhoneNumber)
            }
            
            val contactToSave: Contact
            if (canonicalContact == null) {
                // No canonical contact exists, create a new one owned by the current user.
                contactToSave = Contact(
                    id = UUID.randomUUID().toString(),
                    name = selectedContactInfo.name,
                    phone = selectedContactInfo.phone,
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
                date = state.date!!,
                dueDate = state.dueDate,
                description = state.description
            )
            recordRepository.upsert(recordToSave)
            createRecurringTemplateIfNeeded(user.id, type.id, contactToSave.id)

            Log.e("AddTransactionViewModel", "handleSubmit: $recordToSave")

            // Clear form fields after successful save
            _uiState.update { currentState ->
                currentState.copy(
                    submissionSuccessful = true,
                    isLoading = false,
                    // Clear the form fields
                    contact = null,
                    amountInput = "",
                    amount = null,
                    description = null,
                    dueDate = null,
                    isRecurringEnabled = false,
                    recurrenceType = RecurrenceType.NONE,
                    customRecurrenceDays = ""
                    // Note: We keep type and date as they might want to add similar transactions
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
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.AmountEntered -> {
                _uiState.value = _uiState.value.copy(
                    amountInput = event.amount,
                    amount = event.amount.toIntOrNull(),
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.DateEntered -> {
                _uiState.value = _uiState.value.copy(
                    date = event.date,
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
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
            is AddTransactionUiEvents.RecurringEnabledChanged -> {
                _uiState.value = _uiState.value.copy(
                    isRecurringEnabled = event.isEnabled,
                    recurrenceType = if (event.isEnabled) _uiState.value.recurrenceType else RecurrenceType.NONE,
                    customRecurrenceDays = if (event.isEnabled) _uiState.value.customRecurrenceDays else "",
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.RecurrenceSelected -> {
                _uiState.value = _uiState.value.copy(
                    isRecurringEnabled = event.recurrenceType != RecurrenceType.NONE,
                    recurrenceType = event.recurrenceType,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.CustomRecurrenceDaysEntered -> {
                _uiState.value = _uiState.value.copy(
                    customRecurrenceDays = event.days,
                    submissionSuccessful = false,
                    errorMessage = null
                )
            }
            is AddTransactionUiEvents.ContactSelected -> {
                _uiState.value = _uiState.value.copy(
                    contact = event.contact,
                    // Clear any previous success/error states when user starts new input
                    submissionSuccessful = false,
                    errorMessage = null
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

    private suspend fun createRecurringTemplateIfNeeded(userId: String, typeId: Int, contactId: String) {
        val state = _uiState.value
        if (!state.isRecurringEnabled) return
        val recurrenceType = state.recurrenceType
        if (recurrenceType == RecurrenceType.NONE) return

        val intervalDays = when (recurrenceType) {
            RecurrenceType.NONE -> null
            RecurrenceType.DAILY -> RecurrenceType.DAILY.intervalDays
            RecurrenceType.WEEKLY -> RecurrenceType.WEEKLY.intervalDays
            RecurrenceType.MONTHLY -> RecurrenceType.MONTHLY.intervalDays
            RecurrenceType.CUSTOM -> state.customRecurrenceDays.toIntOrNull()?.takeIf { it > 0 }
        } ?: throw IllegalArgumentException("Custom recurrence interval must be greater than 0 days.")

        val baseDate = state.date ?: System.currentTimeMillis()
        val nextRunAt = baseDate + TimeUnit.DAYS.toMillis(intervalDays.toLong())
        val dueOffsetDays = state.dueDate?.let { dueDate ->
            ((dueDate - baseDate) / TimeUnit.DAYS.toMillis(1)).toInt().coerceAtLeast(0)
        } ?: 0

        val template = RecurringTemplate(
            id = UUID.randomUUID().toString(),
            userId = userId,
            contactId = contactId,
            typeId = typeId,
            amount = state.amount ?: 0,
            description = state.description,
            intervalDays = intervalDays,
            nextRunAt = nextRunAt,
            dueOffsetDays = dueOffsetDays
        )
        recurringTemplateRepository.upsert(template)
    }

    private fun validate(state: AddTransactionUiState): String? {
        if (state.contact == null && state.amountInput.isBlank()) {
            return context.getString(R.string.error_write_something)
        }
        if (state.contact == null) {
            return context.getString(R.string.error_select_contact)
        }
        if (state.amountInput.isBlank()) {
            return context.getString(R.string.error_enter_amount)
        }
        val parsedAmount = state.amountInput.toIntOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            return context.getString(R.string.error_enter_valid_amount)
        }
        if (state.date == null) {
            return context.getString(R.string.error_select_date)
        }
        if (state.isRecurringEnabled && state.recurrenceType == RecurrenceType.CUSTOM) {
            val customDays = state.customRecurrenceDays.toIntOrNull()
            if (customDays == null || customDays <= 0) {
                return context.getString(R.string.error_custom_recurrence_days)
            }
        }
        return null
    }

}
