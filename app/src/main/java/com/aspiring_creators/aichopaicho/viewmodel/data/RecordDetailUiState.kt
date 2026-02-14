package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import com.aspiring_creators.aichopaicho.data.entity.Type

data class RecordDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loan: Loan? = null,
    val repayments: List<Repayment> = emptyList(),
    val remainingBalance: Double = 0.0,
    val contact: Contact? = null,
    val type: Type? = null,
    val isRecordDeleted: Boolean = false
)