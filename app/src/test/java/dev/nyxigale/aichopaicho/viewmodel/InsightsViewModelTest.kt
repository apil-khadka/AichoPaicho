package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Repayment
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private lateinit var viewModel: InsightsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val mockContext = mock<Context> {
            on { getString(any(), any()) } doReturn "Fake Error Message"
            on { getString(R.string.unknown) } doReturn "Unknown"
        }

        val mockRecordRepo = mock<RecordRepository> {
            on { getRecordsByDateRange(any(), any()) } doReturn emptyFlow()
        }

        val mockContactRepo = mock<ContactRepository> {
            on { getAllContacts() } doReturn emptyFlow()
        }

        viewModel = InsightsViewModel(mockContext, mockRecordRepo, mockContactRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun calculateCurrentMonth_returnsCorrectMetrics() {
        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 15)
        }.timeInMillis

        val previousMonth = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.timeInMillis

        val records = listOf(
            createMockRecordWithRepayments(amount = 1000, typeId = 0, date = currentMonth),
            createMockRecordWithRepayments(amount = 2000, typeId = 0, date = currentMonth),
            createMockRecordWithRepayments(amount = 1500, typeId = 1, date = currentMonth),
            createMockRecordWithRepayments(amount = 500, typeId = 1, date = currentMonth),
            createMockRecordWithRepayments(amount = 10000, typeId = 0, date = previousMonth),
            createMockRecordWithRepayments(amount = 10000, typeId = 1, date = previousMonth)
        )

        val result = viewModel.calculateCurrentMonth(records)

        assertEquals(3000.0, result.inflow, 0.0)
        assertEquals(2000.0, result.outflow, 0.0)
    }

    @Test
    fun calculateOverdueOutstanding_returnsCorrectAmount() {
        val now = System.currentTimeMillis()
        val pastDueDate = now - 86400000
        val futureDueDate = now + 86400000

        val records = listOf(
            createMockRecordWithRepayments(amount = 1000, dueDate = pastDueDate, isComplete = false),
            createMockRecordWithRepayments(amount = 1000, dueDate = pastDueDate, isComplete = false, repaymentAmount = 500),
            createMockRecordWithRepayments(amount = 1000, dueDate = pastDueDate, isComplete = true),
            createMockRecordWithRepayments(amount = 1000, dueDate = pastDueDate, isComplete = false, repaymentAmount = 1000),
            createMockRecordWithRepayments(amount = 1000, dueDate = futureDueDate, isComplete = false),
            createMockRecordWithRepayments(amount = 1000, dueDate = null, isComplete = false)
        )

        val result = viewModel.calculateOverdueOutstanding(records)

        assertEquals(1500.0, result, 0.0)
    }

    private fun createMockRecordWithRepayments(
        amount: Int,
        typeId: Int = 0,
        date: Long = System.currentTimeMillis(),
        dueDate: Long? = null,
        isComplete: Boolean = false,
        repaymentAmount: Int = 0
    ): RecordWithRepayments {
        val record = Record(
            id = "test_id",
            userId = "user",
            contactId = "contact",
            typeId = typeId,
            amount = amount,
            date = date,
            dueDate = dueDate,
            isComplete = isComplete,
            description = "Test record"
        )
        val repayments = if (repaymentAmount > 0) {
            listOf(dev.nyxigale.aichopaicho.data.entity.Repayment(
                recordId = "test_id",
                amount = repaymentAmount,
                date = System.currentTimeMillis(),
                description = "Test repayment"
            ))
        } else {
            emptyList()
        }
        return RecordWithRepayments(record, repayments)
    }
}
