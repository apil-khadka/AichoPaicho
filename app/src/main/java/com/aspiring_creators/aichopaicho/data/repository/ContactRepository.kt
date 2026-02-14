package com.aspiring_creators.aichopaicho.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.aspiring_creators.aichopaicho.data.dao.ContactDao
import com.aspiring_creators.aichopaicho.data.entity.Contact
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(private val contactDao: ContactDao) {

    suspend fun checkAndInsert(contact: Contact) : Boolean {
        try {
            if (contact.externalRef != null) {
                val existing = contactDao.getContactByExternalRef(contact.externalRef)
                if (existing != null) return true
            }

            if (contact.normalizedPhone != null) {
                val existingPhone = contactDao.getContactByPhone(contact.normalizedPhone)
                if (existingPhone != null) {
                     if (existingPhone.externalRef == null && contact.externalRef != null) {
                         contactDao.updateContact(existingPhone.copy(externalRef = contact.externalRef))
                     }
                     return true
                }
            }

            contactDao.insert(contact)
            return true
        } catch (e: SQLiteConstraintException) {
            return false
        } catch (e: Exception) {
            return false
        }
    }

     suspend fun getContactByExternalRef(externalRef: String): Contact? {
        return contactDao.getContactByExternalRef(externalRef)
    }

    suspend fun getContactByPhone(normalizedPhone: String): Contact? {
        return contactDao.getContactByPhone(normalizedPhone)
    }

    suspend fun getContactByContactId(contactId: String): Contact? {
        // Fallback or alias
        return contactDao.getContactByExternalRef(contactId) ?: contactDao.getContactById(contactId)
    }

    fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts()
    }

    suspend fun getContactById(id: String): Contact? {
        return contactDao.getContactById(id)
    }

    suspend fun insertContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(id: String) {
        contactDao.deleteContact(id)
    }

    suspend fun updateUserId(oldUserId: String, newUserId: String) {
     contactDao.updateUserId(oldUserId, newUserId)
    }
}
