package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Type

data class ContactTransactionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val contact: Contact? = null,

    // This is the key change to support repayments
    val allRecords: List<RecordWithRepayments> = emptyList(),
    val lentRecords: List<RecordWithRepayments> = emptyList(),
    val borrowedRecords: List<RecordWithRepayments> = emptyList(),

    val types: Map<Int, Type> = emptyMap(),

    // Gross totals of all transactions ever made
    val totalLent: Double = 0.0,
    val totalBorrowed: Double = 0.0,

    // Net balance of what is currently outstanding
    val netBalance: Double = 0.0,

    val showCompleted: Boolean = true,
    val selectedTab: Int = 0
)
