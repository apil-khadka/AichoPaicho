package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary

data class DashboardScreenUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false,
    val recordSummary: UserRecordSummary? = null,
)
