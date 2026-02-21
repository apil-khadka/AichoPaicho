package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Type
import dev.nyxigale.aichopaicho.data.entity.UserRecordSummary


data class ViewTransactionViewModelUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recordSummary: UserRecordSummary? = null,
    val records: List<RecordWithRepayments> = emptyList(),
    val filteredRecords: List<RecordWithRepayments> = emptyList(),
    val contacts: Map<String, Contact> = emptyMap(),
    val types: Map<Int, Type> = emptyMap(),
    val dateRange: Pair<Long, Long> = 0L to 0L,
    val selectedType: Int? = null,
    val fromQuery: String = "",
    val moneyToQuery: String = "",
    val showCompleted: Boolean = false,
    val lentContacts: List<ContactPreview> = emptyList(),
    val borrowedContacts: List<ContactPreview> = emptyList()
)
