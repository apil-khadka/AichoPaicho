package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ContactListViewModel
    private lateinit var contactRepository: ContactRepository
    private lateinit var recordRepository: RecordRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = mock {
            on { getAllContacts() } doReturn flowOf(emptyList())
        }
        recordRepository = mock {
            on { getAllRecords() } doReturn flowOf(emptyList())
        }
        context = mock {
            on { getString(eq(R.string.failed_to_load_contacts), any()) } doReturn "Failed to load contacts: Network Error"
            on { getString(eq(R.string.failed_to_load_records), any()) } doReturn "Failed to load records: Database Error"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts handles error and updates uiState`() = runTest {
        // Arrange
        val errorMessage = "Network Error"
        val expectedMessage = "Failed to load contacts: Network Error"

        whenever(contactRepository.getAllContacts()).thenReturn(flow {
            throw Exception(errorMessage)
        })

        // Act
        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(expectedMessage, state.errorMessage)
        assertEquals(false, state.isLoading)
        assertEquals(emptyList<Contact>(), state.contacts)
    }

    @Test
    fun `loadRecords handles error and updates uiState`() = runTest {
        // Arrange
        val errorMessage = "Database Error"
        val expectedMessage = "Failed to load records: Database Error"

        whenever(recordRepository.getAllRecords()).thenReturn(flow {
            throw Exception(errorMessage)
        })

        // Act
        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(expectedMessage, state.errorMessage)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `clearErrorMessage sets errorMessage to null in uiState`() = runTest {
        // Arrange
        val errorMessage = "Network Error"

        whenever(contactRepository.getAllContacts()).thenReturn(flow {
            throw Exception(errorMessage)
        })

        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle() // This will trigger the exception and set the error message

        // Verify error message is set first
        assertEquals("Failed to load contacts: Network Error", viewModel.uiState.value.errorMessage)

        // Act
        viewModel.clearErrorMessage()

        // Assert
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
