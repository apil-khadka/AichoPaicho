package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.Contact

data class AddTransactionUiState (
    val type: String? = null,
    val contact: Contact? = null,
    val amount: Int? = null,
    val date: Long? = null,
    val description: String? = null,

    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val submissionSuccessful: Boolean = false
)