package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import dev.nyxigale.aichopaicho.viewmodel.data.InsightsUiState
import dev.nyxigale.aichopaicho.viewmodel.data.MonthlyTrendPoint
import dev.nyxigale.aichopaicho.viewmodel.data.TopContactInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltViewModel
class InsightsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun loadInsights() {
        viewModelScope.launch {
            combine(
                recordRepository.getRecordsByDateRange(Long.MIN_VALUE, Long.MAX_VALUE),
                contactRepository.getAllContacts()
            ) { records, contacts ->
                records to contacts
            }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = context.getString(R.string.failed_to_load_records, e.message)
                    )
                }
                .collect { (records, contacts) ->
                    val contactMap = contacts.associateBy { it.id }
                    val monthMetrics = calculateCurrentMonth(records)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        monthlyInflow = monthMetrics.inflow,
                        monthlyOutflow = monthMetrics.outflow,
                        overdueOutstanding = calculateOverdueOutstanding(records),
                        topContacts = calculateTopContacts(records, contactMap),
                        trend = calculateTrend(records),
                        errorMessage = null
                    )
                }
        }
    }

    @VisibleForTesting
    internal fun calculateCurrentMonth(records: List<RecordWithRepayments>): MonthMetrics {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthRecords = records.filter { it.record.date >= monthStart }
        val inflow = monthRecords
            .filter { it.record.typeId == TypeConstants.BORROWED_ID }
            .sumOf { it.record.amount.toDouble() }
        val outflow = monthRecords
            .filter { it.record.typeId == TypeConstants.LENT_ID }
            .sumOf { it.record.amount.toDouble() }

        return MonthMetrics(inflow = inflow, outflow = outflow)
    }

    @VisibleForTesting
    internal fun calculateOverdueOutstanding(records: List<RecordWithRepayments>): Double {
        val now = System.currentTimeMillis()
        return records
            .filter { it.record.dueDate != null && it.record.dueDate < now && !it.isSettled }
            .sumOf { it.remainingAmount.toDouble() }
    }

    private fun calculateTopContacts(
        records: List<RecordWithRepayments>,
        contactMap: Map<String, Contact>
    ): List<TopContactInsight> {
        data class Outstanding(var lent: Double = 0.0, var borrowed: Double = 0.0)

        val aggregates = mutableMapOf<String, Outstanding>()
        records.forEach { recordWithRepayments ->
            val record = recordWithRepayments.record
            val contactId = record.contactId ?: return@forEach
            val remaining = recordWithRepayments.remainingAmount.coerceAtLeast(0).toDouble()
            val aggregate = aggregates.getOrPut(contactId) { Outstanding() }

            if (record.typeId == TypeConstants.LENT_ID) {
                aggregate.lent += remaining
            } else if (record.typeId == TypeConstants.BORROWED_ID) {
                aggregate.borrowed += remaining
            }
        }

        return aggregates.map { (contactId, outstanding) ->
            val contactName = contactMap[contactId]?.name ?: context.getString(R.string.unknown)
            TopContactInsight(
                contactId = contactId,
                name = contactName,
                netBalance = outstanding.lent - outstanding.borrowed,
                lentOutstanding = outstanding.lent,
                borrowedOutstanding = outstanding.borrowed
            )
        }.sortedByDescending { abs(it.netBalance) }
            .take(5)
    }

    private fun calculateTrend(records: List<RecordWithRepayments>): List<MonthlyTrendPoint> {
        val monthFormatter = SimpleDateFormat("MMM yy", Locale.getDefault())
        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        data class MonthKey(val year: Int, val month: Int)
        data class FlowMetrics(var inflow: Double = 0.0, var outflow: Double = 0.0)

        val monthKeys = (5 downTo 0).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = currentMonth.timeInMillis
                add(Calendar.MONTH, -offset)
            }
        }
        val metricMap = monthKeys.associate {
            MonthKey(it.get(Calendar.YEAR), it.get(Calendar.MONTH)) to FlowMetrics()
        }.toMutableMap()

        records.forEach { recordWithRepayments ->
            val record = recordWithRepayments.record
            val recordCalendar = Calendar.getInstance().apply { timeInMillis = record.date }
            val key = MonthKey(
                year = recordCalendar.get(Calendar.YEAR),
                month = recordCalendar.get(Calendar.MONTH)
            )
            val metrics = metricMap[key] ?: return@forEach

            if (record.typeId == TypeConstants.BORROWED_ID) {
                metrics.inflow += record.amount.toDouble()
            } else if (record.typeId == TypeConstants.LENT_ID) {
                metrics.outflow += record.amount.toDouble()
            }
        }

        return monthKeys.map { month ->
            val key = MonthKey(month.get(Calendar.YEAR), month.get(Calendar.MONTH))
            val metrics = metricMap[key] ?: FlowMetrics()
            MonthlyTrendPoint(
                label = monthFormatter.format(month.time),
                inflow = metrics.inflow,
                outflow = metrics.outflow
            )
        }
    }

    @VisibleForTesting
    internal data class MonthMetrics(
        val inflow: Double,
        val outflow: Double
    )
}
