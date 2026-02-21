package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Type

data class RecordDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recordWithRepayments: RecordWithRepayments? = null,
    val contact: Contact? = null,
    val type: Type? = null,
    val isRecordDeleted: Boolean = false,

    // For the new repayment form
    val repaymentAmount: String = "",
    val repaymentDescription: String = "",
    val repaymentSaved: Boolean = false
)
