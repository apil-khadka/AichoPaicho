package dev.nyxigale.aichopaicho.viewmodel.data

data class MonthlyTrendPoint(
    val label: String,
    val inflow: Double,
    val outflow: Double
) {
    val net: Double
        get() = inflow - outflow
}

data class TopContactInsight(
    val contactId: String,
    val name: String,
    val netBalance: Double,
    val lentOutstanding: Double,
    val borrowedOutstanding: Double
)

data class InsightsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val monthlyInflow: Double = 0.0,
    val monthlyOutflow: Double = 0.0,
    val overdueOutstanding: Double = 0.0,
    val topContacts: List<TopContactInsight> = emptyList(),
    val trend: List<MonthlyTrendPoint> = emptyList()
)
