package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.Contact

data class AddTransactionUiState (
    val type: String? = null,
    val contact: Contact? = null,
    val amountInput: String = "",
    val amount: Int? = null,
    val date: Long? = null,
    val dueDate: Long? = null,
    val description: String? = null,
    val isRecurringEnabled: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val customRecurrenceDays: String = "",

    val contactError: String? = null,
    val amountError: String? = null,
    val dateError: String? = null,
    val customRecurrenceDaysError: String? = null,

    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val submissionSuccessful: Boolean = false
)
