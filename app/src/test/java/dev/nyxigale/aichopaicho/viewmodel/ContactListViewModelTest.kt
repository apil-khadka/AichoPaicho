package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ContactListViewModel
    private lateinit var contactRepository: ContactRepository
    private lateinit var recordRepository: RecordRepository
    private lateinit var context: Context

    private val mockContacts = listOf(
        Contact(id = "1", name = "Alice", userId = null, phone = listOf(), contactId = "c1"),
        Contact(id = "2", name = "Bob", userId = null, phone = listOf(), contactId = "c2"),
        Contact(id = "3", name = "Charlie", userId = null, phone = listOf(), contactId = "c3"),
        Contact(id = "4", name = "alice2", userId = null, phone = listOf(), contactId = "c4")
    )

    private val mockRecords = listOf(
        Record(id = "r1", contactId = "1", typeId = TypeConstants.LENT_ID, amount = 100, description = "test", isDeleted = false, userId = "u1", date = 1L),
        Record(id = "r2", contactId = "2", typeId = TypeConstants.BORROWED_ID, amount = 50, description = "test", isDeleted = false, userId = "u1", date = 1L),
        Record(id = "r3", contactId = "3", typeId = TypeConstants.LENT_ID, amount = 20, description = "test", isDeleted = false, userId = "u1", date = 1L),
        Record(id = "r4", contactId = "4", typeId = TypeConstants.LENT_ID, amount = 30, description = "test", isDeleted = false, userId = "u1", date = 1L)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        contactRepository = mockk()
        recordRepository = mockk()

        // Default behavior
        every { contactRepository.getAllContacts() } returns flowOf(mockContacts)
        every { recordRepository.getAllRecords() } returns flowOf(mockRecords)
        every { context.getString(any()) } returns "Mock String"
        every { context.getString(any(), any()) } returns "Mock String with Args"

        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchContacts_updatesSearchQuery() = runTest {
        advanceUntilIdle()
        viewModel.searchContacts("ali")
        assertEquals("ali", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun testGetFilteredContacts_withSearchQuery_returnsFilteredContacts() = runTest {
        advanceUntilIdle()
        viewModel.searchContacts("ali")
        val result = viewModel.getFilteredContacts("")
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Alice" })
        assertTrue(result.any { it.name == "alice2" })
    }

    @Test
    fun testJumpToLetter_updatesSelectedLetter() = runTest {
        advanceUntilIdle()
        viewModel.jumpToLetter("C")
        assertEquals("C", viewModel.uiState.value.selectedLetter)
    }

    @Test
    fun testGetFilteredContacts_withSelectedLetter_returnsFilteredContacts() = runTest {
        advanceUntilIdle()
        viewModel.jumpToLetter("C")
        val result = viewModel.getFilteredContacts("")
        assertEquals(1, result.size)
        assertEquals("Charlie", result[0].name)
    }

    @Test
    fun `loadContacts handles error and updates uiState`() = runTest {
        val errorMessage = "Network Error"
        every { contactRepository.getAllContacts() } returns flow { throw Exception(errorMessage) }
        
        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.errorMessage != null)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `loadRecords handles error and updates uiState`() = runTest {
        val errorMessage = "Database Error"
        every { recordRepository.getAllRecords() } returns flow { throw Exception(errorMessage) }

        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.errorMessage != null)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `clearErrorMessage sets errorMessage to null in uiState`() = runTest {
        every { contactRepository.getAllContacts() } returns flow { throw Exception("Error") }
        viewModel = ContactListViewModel(context, contactRepository, recordRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)
        viewModel.clearErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
