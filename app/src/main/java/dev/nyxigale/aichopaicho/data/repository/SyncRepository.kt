package dev.nyxigale.aichopaicho.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.Repayment
import dev.nyxigale.aichopaicho.data.sync.SyncEntityType
import dev.nyxigale.aichopaicho.data.sync.SyncFailureItem
import dev.nyxigale.aichopaicho.data.sync.SyncReport
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val contactRepository: ContactRepository,
    private val recordRepository: RecordRepository,
    private val repaymentRepository: RepaymentRepository,
    private val firebaseAuth: FirebaseAuth,
) {
    suspend fun estimateQueueCount(): Int {
        return runCatching {
            val contactsCount = contactRepository.getAllContacts().first().size
            val records = recordRepository.getAllRecords().first()
            val repaymentsCount = records.sumOf { record ->
                repaymentRepository.getRepaymentsForRecord(record.id).first().size
            }
            contactsCount + records.size + repaymentsCount + 1
        }.getOrElse { error ->
            Log.e(TAG, "Failed to estimate queue count", error)
            0
        }
    }

    suspend fun syncAllUploads(): SyncReport {
        return syncContacts() + syncRecords() + syncRepayments() + syncUserData()
    }

    suspend fun syncContacts(): SyncReport {
        val user = userRepository.getUser()
        if (user.id.isBlank()) {
            return SyncReport.EMPTY
        }
        val contacts = runCatching { contactRepository.getAllContacts().first() }
            .getOrElse { error ->
                Log.e(TAG, "Failed to read contacts for sync", error)
                return SyncReport(
                    attempted = 1,
                    succeeded = 0,
                    failed = 1,
                    failedItems = listOf(
                        SyncFailureItem(
                            entityType = SyncEntityType.CONTACT,
                            entityId = "contacts-read",
                            reason = error.message
                        )
                    )
                )
            }

        var report = SyncReport.EMPTY
        coroutineScope {
            val jobs = contacts.map { contact ->
                async {
                    syncSingleContactReport(user.id, contact)
                }
            }
            jobs.awaitAll().forEach { report += it }
        }
        return report
    }

    suspend fun syncRecords(): SyncReport {
        val user = userRepository.getUser()
        if (user.id.isBlank()) {
            return SyncReport.EMPTY
        }
        val records = runCatching { recordRepository.getAllRecords().first() }
            .getOrElse { error ->
                Log.e(TAG, "Failed to read records for sync", error)
                return SyncReport(
                    attempted = 1,
                    succeeded = 0,
                    failed = 1,
                    failedItems = listOf(
                        SyncFailureItem(
                            entityType = SyncEntityType.RECORD,
                            entityId = "records-read",
                            reason = error.message
                        )
                    )
                )
            }

        var report = SyncReport.EMPTY
        records.forEach { record ->
            report += syncSingleRecordReport(user.id, record)
        }
        return report
    }

    suspend fun syncRepayments(): SyncReport {
        val user = userRepository.getUser()
        if (user.id.isBlank()) {
            return SyncReport.EMPTY
        }

        val records = runCatching { recordRepository.getAllRecords().first() }
            .getOrElse { error ->
                Log.e(TAG, "Failed to read records for repayment sync", error)
                return SyncReport(
                    attempted = 1,
                    succeeded = 0,
                    failed = 1,
                    failedItems = listOf(
                        SyncFailureItem(
                            entityType = SyncEntityType.REPAYMENT,
                            entityId = "repayments-read",
                            reason = error.message
                        )
                    )
                )
            }

        var report = SyncReport.EMPTY
        records.forEach { record ->
            val repayments = runCatching {
                repaymentRepository.getRepaymentsForRecord(record.id).first()
            }.getOrElse { error ->
                report += SyncReport(
                    attempted = 1,
                    succeeded = 0,
                    failed = 1,
                    failedItems = listOf(
                        SyncFailureItem(
                            entityType = SyncEntityType.REPAYMENT,
                            entityId = "record:${record.id}",
                            reason = error.message
                        )
                    )
                )
                emptyList()
            }

            repayments.forEach { repayment ->
                report += syncSingleRepaymentReport(user.id, repayment)
            }
        }
        return report
    }

    suspend fun syncUserData(): SyncReport {
        val user = userRepository.getUser()
        if (user.id.isBlank()) {
            return SyncReport.EMPTY
        }
        return try {
            userRepository.upsertRemoteUser(user)
            SyncReport(attempted = 1, succeeded = 1, failed = 0)
        } catch (error: Exception) {
            Log.e(TAG, "Failed syncing user data", error)
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.USER,
                        entityId = user.id,
                        reason = error.message
                    )
                )
            )
        }
    }

    suspend fun retryFailedItems(failedItems: List<SyncFailureItem>): SyncReport {
        val user = userRepository.getUser()
        if (user.id.isBlank()) {
            return SyncReport.EMPTY
        }

        var report = SyncReport.EMPTY
        failedItems.distinctBy { it.key() }.forEach { failedItem ->
            when (failedItem.entityType) {
                SyncEntityType.CONTACT -> {
                    report += syncSingleContactByIdReport(user.id, failedItem.entityId)
                }

                SyncEntityType.RECORD -> {
                    report += syncSingleRecordByIdReport(user.id, failedItem.entityId)
                }

                SyncEntityType.REPAYMENT -> {
                    report += syncSingleRepaymentByIdReport(user.id, failedItem.entityId)
                }

                SyncEntityType.USER -> {
                    report += syncUserData()
                }
            }
        }
        return report
    }

    suspend fun syncSingleContact(contactId: String) {
        val user = userRepository.getUser()
        if (user.id.isBlank()) return
        syncSingleContactByIdReport(user.id, contactId)
    }

    suspend fun syncSingleRecord(recordId: String) {
        val user = userRepository.getUser()
        if (user.id.isBlank()) return
        syncSingleRecordByIdReport(user.id, recordId)
    }

    suspend fun syncSingleRepayment(repaymentId: String) {
        val user = userRepository.getUser()
        if (user.id.isBlank()) return
        syncSingleRepaymentByIdReport(user.id, repaymentId)
    }

    suspend fun downloadAndMergeData() = withContext(Dispatchers.IO) {
        try {
            val user = userRepository.getUser()
            if (user.id.isBlank()) {
                Log.d(TAG, "User ID is blank. Skipping download.")
                return@withContext
            }
            if (firebaseAuth.currentUser?.uid != user.id) {
                Log.d(TAG, "Current Firebase Auth user does not match local user. Skipping download.")
                return@withContext
            }

            // 1. Sync Contacts
            try {
                val contactsSnapshot = firestore.collection("users")
                    .document(user.id)
                    .collection("contacts")
                    .get()
                    .await()

                val firestoreContactIds = contactsSnapshot.documents
                    .mapNotNull { it.getString("id") }
                    .filter { it.isNotEmpty() }

                val localContactsMap = contactRepository.getContactsByIds(firestoreContactIds)
                    .associateBy { it.id }

                val contactsToInsertOrUpdate = mutableListOf<Contact>()
                val contactsToUpload = mutableListOf<String>()

                for (doc in contactsSnapshot.documents) {
                    val firestoreId = doc.getString("id") ?: ""
                    if (firestoreId.isEmpty()) continue

                    val firestoreContact = Contact(
                        id = firestoreId,
                        name = doc.getString("name") ?: "",
                        userId = doc.getString("userId"),
                        phone = doc.get("phone") as? List<String?> ?: emptyList(),
                        contactId = doc.getString("contactId"),
                        isDeleted = doc.getBoolean("deleted") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )

                    val localContact = localContactsMap[firestoreContact.id]
                    if (localContact == null || firestoreContact.updatedAt > localContact.updatedAt) {
                        contactsToInsertOrUpdate.add(firestoreContact)
                    } else if (localContact.updatedAt > firestoreContact.updatedAt) {
                        contactsToUpload.add(localContact.id)
                    }
                }
                if (contactsToInsertOrUpdate.isNotEmpty()) {
                    contactRepository.insertContacts(contactsToInsertOrUpdate)
                }
                for (id in contactsToUpload) syncSingleContact(id)
            } catch (error: Exception) {
                Log.e(TAG, "Error downloading contacts", error)
            }

            // 2. Sync Records and Repayments
            try {
                val recordsSnapshot = firestore.collection("users")
                    .document(user.id)
                    .collection("records")
                    .get()
                    .await()

                val firestoreRecords = mutableListOf<Record>()
                val validRecordIds = mutableListOf<String>()
                val allRepaymentsToInsert = mutableListOf<Repayment>()

                for (doc in recordsSnapshot.documents) {
                    val firestoreId = doc.getString("id") ?: ""
                    if (firestoreId.isEmpty()) continue

                    val firestoreRecord = Record(
                        id = firestoreId,
                        userId = doc.getString("userId"),
                        contactId = doc.getString("contactId"),
                        typeId = doc.getLong("typeId")?.toInt() ?: 0,
                        amount = doc.getLong("amount")?.toInt() ?: 0,
                        date = doc.getLong("date") ?: 0L,
                        dueDate = doc.getLong("dueDate"),
                        isComplete = doc.getBoolean("complete") ?: false,
                        isDeleted = doc.getBoolean("deleted") ?: false,
                        description = doc.getString("description"),
                        recurringTemplateId = doc.getString("recurringTemplateId"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                    firestoreRecords.add(firestoreRecord)
                    validRecordIds.add(firestoreId)
                }

                // Resolve Records
                if (validRecordIds.isNotEmpty()) {
                    val localRecords = recordRepository.getRecordsByIds(validRecordIds).associateBy { it.id }
                    val recordsToInsert = mutableListOf<Record>()
                    val recordsToUpdate = mutableListOf<Record>()

                    for (firestoreRecord in firestoreRecords) {
                        val localRecord = localRecords[firestoreRecord.id]
                        if (localRecord == null) {
                            recordsToInsert.add(firestoreRecord)
                        } else if (firestoreRecord.updatedAt > localRecord.updatedAt) {
                            recordsToUpdate.add(firestoreRecord)
                        } else if (localRecord.updatedAt > firestoreRecord.updatedAt) {
                            syncSingleRecord(localRecord.id)
                        }
                    }

                    if (recordsToInsert.isNotEmpty()) {
                        recordRepository.insertRecords(recordsToInsert)
                    }
                    if (recordsToUpdate.isNotEmpty()) {
                        recordRepository.updateRecords(recordsToUpdate)
                    }
                }

                // Resolve Repayments in parallel
                coroutineScope {
                    val repaymentsDeferred = validRecordIds.map { recordId ->
                        async {
                            runCatching {
                                firestore.collection("users")
                                    .document(user.id)
                                    .collection("records")
                                    .document(recordId)
                                    .collection("repayments")
                                    .get()
                                    .await()
                                    .documents
                            }.getOrElse { emptyList() }
                        }
                    }

                    repaymentsDeferred.awaitAll().flatten().forEach { doc ->
                        val firestoreRepaymentId = doc.getString("id") ?: ""
                        if (firestoreRepaymentId.isEmpty()) return@forEach

                        val firestoreRepaymentObject = doc.toObject(Repayment::class.java) ?: return@forEach
                        val firestoreRepayment = firestoreRepaymentObject.copy(
                            updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                        )

                        val localRepayment = repaymentRepository.getRepaymentById(firestoreRepaymentId)
                        if (localRepayment == null || firestoreRepayment.updatedAt > localRepayment.updatedAt) {
                            allRepaymentsToInsert.add(firestoreRepayment)
                        } else if (localRepayment.updatedAt > firestoreRepayment.updatedAt) {
                            syncSingleRepayment(localRepayment.id)
                        }
                    }
                }

                if (allRepaymentsToInsert.isNotEmpty()) {
                    repaymentRepository.insertRepayments(allRepaymentsToInsert)
                }
            } catch (error: Exception) {
                Log.e(TAG, "Error downloading records or repayments", error)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Error in downloadAndMergeData", error)
        }
    }

    private suspend fun syncSingleContactByIdReport(userId: String, contactId: String): SyncReport {
        val contact = contactRepository.getContactById(contactId)
        return if (contact == null) {
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.CONTACT,
                        entityId = contactId,
                        reason = "Contact not found locally"
                    )
                )
            )
        } else {
            syncSingleContactReport(userId, contact)
        }
    }

    private suspend fun syncSingleRecordByIdReport(userId: String, recordId: String): SyncReport {
        val record = recordRepository.getRecordById(recordId)
        return if (record == null) {
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.RECORD,
                        entityId = recordId,
                        reason = "Record not found locally"
                    )
                )
            )
        } else {
            syncSingleRecordReport(userId, record)
        }
    }

    private suspend fun syncSingleRepaymentByIdReport(
        userId: String,
        repaymentId: String
    ): SyncReport {
        val repayment = repaymentRepository.getRepaymentById(repaymentId)
        return if (repayment == null) {
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.REPAYMENT,
                        entityId = repaymentId,
                        reason = "Repayment not found locally"
                    )
                )
            )
        } else {
            syncSingleRepaymentReport(userId, repayment)
        }
    }

    private suspend fun syncSingleContactReport(userId: String, contact: Contact): SyncReport {
        return try {
            val contactData = hashMapOf(
                "id" to contact.id,
                "name" to contact.name,
                "phone" to contact.phone,
                "userId" to contact.userId,
                "contactId" to contact.contactId,
                "deleted" to contact.isDeleted,
                "createdAt" to contact.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(userId)
                .collection("contacts")
                .document(contact.id)
                .set(contactData, SetOptions.merge())
                .await()

            SyncReport(attempted = 1, succeeded = 1, failed = 0)
        } catch (error: Exception) {
            Log.e(TAG, "Error syncing contact ${contact.id}", error)
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.CONTACT,
                        entityId = contact.id,
                        reason = error.message
                    )
                )
            )
        }
    }

    private suspend fun syncSingleRecordReport(userId: String, record: Record): SyncReport {
        return try {
            val recordData = hashMapOf(
                "id" to record.id,
                "userId" to record.userId,
                "contactId" to record.contactId,
                "date" to record.date,
                "dueDate" to record.dueDate,
                "deleted" to record.isDeleted,
                "complete" to record.isComplete,
                "description" to record.description,
                "recurringTemplateId" to record.recurringTemplateId,
                "typeId" to record.typeId,
                "amount" to record.amount,
                "createdAt" to record.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(userId)
                .collection("records")
                .document(record.id)
                .set(recordData, SetOptions.merge())
                .await()

            SyncReport(attempted = 1, succeeded = 1, failed = 0)
        } catch (error: Exception) {
            Log.e(TAG, "Error syncing record ${record.id}", error)
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.RECORD,
                        entityId = record.id,
                        reason = error.message
                    )
                )
            )
        }
    }

    private suspend fun syncSingleRepaymentReport(userId: String, repayment: Repayment): SyncReport {
        return try {
            val repaymentData = hashMapOf(
                "id" to repayment.id,
                "recordId" to repayment.recordId,
                "amount" to repayment.amount,
                "date" to repayment.date,
                "description" to repayment.description,
                "createdAt" to repayment.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(userId)
                .collection("records")
                .document(repayment.recordId)
                .collection("repayments")
                .document(repayment.id)
                .set(repaymentData, SetOptions.merge())
                .await()

            SyncReport(attempted = 1, succeeded = 1, failed = 0)
        } catch (error: Exception) {
            Log.e(TAG, "Error syncing repayment ${repayment.id}", error)
            SyncReport(
                attempted = 1,
                succeeded = 0,
                failed = 1,
                failedItems = listOf(
                    SyncFailureItem(
                        entityType = SyncEntityType.REPAYMENT,
                        entityId = repayment.id,
                        reason = error.message
                    )
                )
            )
        }
    }

    companion object {
        private const val TAG = "SyncRepository"
    }
}
