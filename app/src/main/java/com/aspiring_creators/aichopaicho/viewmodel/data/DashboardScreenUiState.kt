package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import com.aspiring_creators.aichopaicho.viewmodel.data.UpcomingDueItem

data class DashboardScreenUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false,
    val recordSummary: UserRecordSummary? = null,
    val upcomingDue: List<UpcomingDueItem> = emptyList(),
)
