package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.User
import dev.nyxigale.aichopaicho.data.entity.UserRecordSummary

data class DashboardScreenUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false,
    val recordSummary: UserRecordSummary? = null,
    val upcomingDue: List<UpcomingDueItem> = emptyList(),
)
