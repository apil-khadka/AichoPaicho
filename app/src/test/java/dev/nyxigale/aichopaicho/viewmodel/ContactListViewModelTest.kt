package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.data.dao.FakeContactDao
import dev.nyxigale.aichopaicho.data.dao.FakeRecordDao
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import dev.nyxigale.aichopaicho.R
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {

    private lateinit var viewModel: ContactListViewModel
    private lateinit var fakeContactDao: FakeContactDao
    private lateinit var fakeRecordDao: FakeRecordDao
    private lateinit var contactRepository: ContactRepository
    private lateinit var recordRepository: RecordRepository

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeContactDao = FakeContactDao()
        fakeRecordDao = FakeRecordDao()
        contactRepository = ContactRepository(fakeContactDao)
        recordRepository = RecordRepository(fakeRecordDao)

        val mockContext = mock(Context::class.java)
        `when`(mockContext.getString(R.string.failed_to_load_contacts, null)).thenReturn("Error")
        `when`(mockContext.getString(R.string.failed_to_load_records, null)).thenReturn("Error")

        viewModel = ContactListViewModel(mockContext, contactRepository, recordRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val testContacts = listOf(
        Contact(id = "1", name = "Alice", userId = "user1", phone = listOf(), contactId = "c1"),
        Contact(id = "2", name = "Bob", userId = "user1", phone = listOf(), contactId = "c2"),
        Contact(id = "3", name = "Charlie", userId = "user1", phone = listOf(), contactId = "c3")
    )

    private val testRecords = listOf(
        Record(id = "r1", userId = "user1", contactId = "1", typeId = TypeConstants.LENT_ID, amount = 100, date = 100L, description = null),
        Record(id = "r2", userId = "user1", contactId = "2", typeId = TypeConstants.BORROWED_ID, amount = 200, date = 200L, description = null)
    )

    @Test
    fun getContactsByType_emptyType_returnsAllContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        val result = viewModel.getContactsByType("")

        assertEquals(3, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Bob", result[1].name)
        assertEquals("Charlie", result[2].name)
    }

    @Test
    fun getContactsByType_lent_returnsLentContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        val result = viewModel.getContactsByType(TypeConstants.TYPE_LENT)

        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun getContactsByType_borrowed_returnsBorrowedContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        val result = viewModel.getContactsByType(TypeConstants.TYPE_BORROWED)

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].name)
    }

    @Test
    fun getFilteredContacts_withSearchQuery_returnsFilteredContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        viewModel.searchContacts("ali")

        val result = viewModel.getFilteredContacts("")

        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun getFilteredContacts_withSelectedLetter_returnsFilteredContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        viewModel.jumpToLetter("B")

        val result = viewModel.getFilteredContacts("")

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].name)
    }

    @Test
    fun getFilteredContacts_withBothFilters_returnsCorrectContacts() = runTest {
        fakeContactDao.emit(testContacts)
        fakeRecordDao.emit(testRecords)



        viewModel.searchContacts("o")
        viewModel.jumpToLetter("B")

        val result = viewModel.getFilteredContacts("")

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].name)

        viewModel.searchContacts("x")
        val resultEmpty = viewModel.getFilteredContacts("")
        assertEquals(0, resultEmpty.size)
    }

    @Test
    fun getAvailableLetters_returnsDistinctSortedLetters() {
        val contacts = listOf(
            Contact(id = "1", name = "Zebra", userId = "u1", phone = listOf(), contactId = "c1"),
            Contact(id = "2", name = "Apple", userId = "u1", phone = listOf(), contactId = "c2"),
            Contact(id = "3", name = "Banana", userId = "u1", phone = listOf(), contactId = "c3"),
            Contact(id = "4", name = "apple2", userId = "u1", phone = listOf(), contactId = "c4")
        )
        val letters = viewModel.getAvailableLetters(contacts)
        assertEquals(listOf("A", "B", "Z"), letters)
    }
}
