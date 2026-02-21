package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact

sealed class AddTransactionUiEvents {

    data class ContactSelected(val contact: Contact) : AddTransactionUiEvents()
    data class AmountEntered(val amount: String) : AddTransactionUiEvents()
    data class DateEntered(val date: Long) : AddTransactionUiEvents()
    data class DueDateEntered(val dueDate: Long?) : AddTransactionUiEvents()
    data class DescriptionEntered(val description: String) : AddTransactionUiEvents()

    data class TypeSelected(val type: String): AddTransactionUiEvents()
    object Submit : AddTransactionUiEvents()
}
