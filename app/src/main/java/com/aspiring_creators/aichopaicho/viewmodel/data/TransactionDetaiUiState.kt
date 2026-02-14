package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.RecordWithRepayments
import com.aspiring_creators.aichopaicho.data.entity.Type

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
