package dev.nyxigale.aichopaicho.data.dao

import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeContactDao : ContactDao {
    private val contactsFlow = MutableStateFlow<List<Contact>>(emptyList())

    fun emit(contacts: List<Contact>) {
        contactsFlow.value = contacts
    }

    override suspend fun insert(contact: Contact): Long = 1L
    override fun getAllContacts(): Flow<List<Contact>> = contactsFlow
    override suspend fun getContactByContactId(contactId: String): Contact? = contactsFlow.value.find { it.contactId == contactId }
    override suspend fun findByPhoneNumber(phoneNumber: String): Contact? = null
    override suspend fun getContactById(contactId: String): Contact? = contactsFlow.value.find { it.id == contactId }
    override suspend fun getContactsByIds(contactIds: List<String>): List<Contact> = contactsFlow.value.filter { it.id in contactIds }
    override suspend fun insertContact(contact: Contact) {}
    override suspend fun insertContacts(contacts: List<Contact>) {}
    override suspend fun updateContact(contact: Contact) {}
    override suspend fun deleteContact(contactId: String, timestamp: Long) {}
    override suspend fun updateUserId(oldUserId: String, newUserId: String) {}
}

class FakeRecordDao : RecordDao {
    private val recordsFlow = MutableStateFlow<List<Record>>(emptyList())

    fun emit(records: List<Record>) {
        recordsFlow.value = records
    }

    override suspend fun upsert(record: Record) {}
    override suspend fun upsertAll(records: List<Record>) {}
    override suspend fun getTotalByType(typeId: Int): Int = 0
    override fun getAllRecords(): Flow<List<Record>> = recordsFlow
    override fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<RecordWithRepayments>> = MutableStateFlow(emptyList())
    override suspend fun getRecordById(recordId: String): Record? = recordsFlow.value.find { it.id == recordId }
    override suspend fun getRecordsByIds(recordIds: List<String>): List<Record> = recordsFlow.value.filter { it.id in recordIds }
    override fun getRecordWithRepaymentsById(recordId: String): Flow<RecordWithRepayments?> = MutableStateFlow(null)
    override fun getRecordsWithRepaymentsByContactId(contactId: String): Flow<List<RecordWithRepayments>> = MutableStateFlow(emptyList())
    override suspend fun updateRecord(record: Record) {}
    override suspend fun updateRecords(records: List<Record>) {}
    override suspend fun deleteRecord(recordId: String, timestamp: Long) {}
    override suspend fun insertRecord(record: Record) {}
    override suspend fun insertRecords(records: List<Record>) {}
    override fun getRecordsByContactId(contactId: String): Flow<List<Record>> = recordsFlow.map { list -> list.filter { it.contactId == contactId } }
    override suspend fun updateUserId(oldUserId: String, newUserId: String) {}
    override suspend fun getOpenDueRecordsUntil(endTime: Long): List<Record> = emptyList()
}
