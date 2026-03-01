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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

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
        contacts.forEach { contact ->
            report += syncSingleContactReport(user.id, contact)
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

    suspend fun downloadAndMergeData() {
        try {
            val user = userRepository.getUser()
            if (user.id.isBlank()) {
                Log.d(TAG, "User ID is blank. Skipping download.")
                return
            }
            if (firebaseAuth.currentUser?.uid != user.id) {
                Log.d(TAG, "Current Firebase Auth user does not match local user. Skipping download.")
                return
            }

            try {
                val contactsSnapshot = firestore.collection("users")
                    .document(user.id)
                    .collection("contacts")
                    .get()
                    .await()

                for (doc in contactsSnapshot.documents) {
                    try {
                        val firestoreId = doc.getString("id") ?: ""
                        val firestoreName = doc.getString("name") ?: ""
                        val firestoreUserId = doc.getString("userId")
                        @Suppress("UNCHECKED_CAST")
                        val firestorePhone = doc.get("phone") as? List<String?> ?: emptyList()
                        val firestoreContactId = doc.getString("contactId")
                        val firestoreIsDeleted = doc.getBoolean("deleted") ?: false
                        val firestoreCreatedAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        val firestoreUpdatedAtTimestamp = doc.getTimestamp("updatedAt")
                        val firestoreUpdatedAt =
                            firestoreUpdatedAtTimestamp?.toDate()?.time ?: System.currentTimeMillis()

                        if (firestoreId.isEmpty()) {
                            Log.w(TAG, "Parsed contact with empty ID from Firestore doc=${doc.id}, skipping")
                            continue
                        }

                        val firestoreContact = Contact(
                            id = firestoreId,
                            name = firestoreName,
                            userId = firestoreUserId,
                            phone = firestorePhone,
                            contactId = firestoreContactId,
                            isDeleted = firestoreIsDeleted,
                            createdAt = firestoreCreatedAt,
                            updatedAt = firestoreUpdatedAt
                        )

                        val localContact = contactRepository.getContactById(firestoreContact.id)
                        if (localContact == null) {
                            contactRepository.insertContact(firestoreContact)
                            Log.d(TAG, "Inserted new contact from Firestore: ${firestoreContact.id}")
                        } else if (firestoreContact.updatedAt > localContact.updatedAt) {
                            contactRepository.updateContact(firestoreContact)
                            Log.d(TAG, "Updated local contact from Firestore: ${firestoreContact.id}")
                        } else if (localContact.updatedAt > firestoreContact.updatedAt) {
                            Log.d(TAG, "Local contact ${localContact.id} is newer. Re-uploading.")
                            syncSingleContact(localContact.id)
                        }
                    } catch (error: Exception) {
                        Log.e(TAG, "Error processing contact document ${doc.id}", error)
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Error downloading contacts", error)
            }

            try {
                val recordsSnapshot = firestore.collection("users")
                    .document(user.id)
                    .collection("records")
                    .get()
                    .await()

                val firestoreRecords = mutableListOf<Record>()
                val validRecordIds = mutableListOf<String>()

                for (doc in recordsSnapshot.documents) {
                    try {
                        val firestoreId = doc.getString("id") ?: ""
                        val firestoreUserId = doc.getString("userId")
                        val firestoreContactId = doc.getString("contactId")
                        val firestoreTypeId = doc.getLong("typeId")?.toInt() ?: 0
                        val firestoreAmount = doc.getLong("amount")?.toInt() ?: 0
                        val firestoreDate = doc.getLong("date") ?: 0L
                        val firestoreDueDate = doc.getLong("dueDate")
                        val firestoreIsComplete = doc.getBoolean("complete") ?: false
                        val firestoreIsDeleted = doc.getBoolean("deleted") ?: false
                        val firestoreDescription = doc.getString("description")
                        val firestoreRecurringTemplateId = doc.getString("recurringTemplateId")
                        val firestoreCreatedAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        val firestoreUpdatedAtTimestamp = doc.getTimestamp("updatedAt")
                        val firestoreUpdatedAt =
                            firestoreUpdatedAtTimestamp?.toDate()?.time ?: System.currentTimeMillis()

                        if (firestoreId.isEmpty()) {
                            Log.w(TAG, "Parsed record with empty ID from Firestore doc=${doc.id}, skipping")
                            continue
                        }

                        val firestoreRecord = Record(
                            id = firestoreId,
                            userId = firestoreUserId,
                            contactId = firestoreContactId,
                            typeId = firestoreTypeId,
                            amount = firestoreAmount,
                            date = firestoreDate,
                            dueDate = firestoreDueDate,
                            isComplete = firestoreIsComplete,
                            isDeleted = firestoreIsDeleted,
                            description = firestoreDescription,
                            recurringTemplateId = firestoreRecurringTemplateId,
                            createdAt = firestoreCreatedAt,
                            updatedAt = firestoreUpdatedAt
                        )

                        firestoreRecords.add(firestoreRecord)
                        validRecordIds.add(firestoreRecord.id)

                        // Keep the repayment logic intact but run it asynchronously later or here
                        val repaymentsSnapshot = firestore.collection("users")
                            .document(user.id)
                            .collection("records")
                            .document(firestoreId)
                            .collection("repayments")
                            .get()
                            .await()

                        for (repaymentDoc in repaymentsSnapshot.documents) {
                            val firestoreRepaymentId = repaymentDoc.getString("id") ?: ""
                            if (firestoreRepaymentId.isEmpty()) {
                                Log.w(TAG, "Parsed repayment with empty ID from Firestore doc=${repaymentDoc.id}, skipping")
                                continue
                            }

                            val firestoreRepaymentObject =
                                repaymentDoc.toObject(Repayment::class.java)
                            if (firestoreRepaymentObject == null) {
                                Log.w(TAG, "Could not parse repayment ${repaymentDoc.id}, skipping")
                                continue
                            }

                            val firestoreRepayment = firestoreRepaymentObject.copy(
                                updatedAt = repaymentDoc.getTimestamp("updatedAt")
                                    ?.toDate()
                                    ?.time
                                    ?: System.currentTimeMillis()
                            )

                            val localRepayment =
                                repaymentRepository.getRepaymentById(firestoreRepaymentId)
                            if (localRepayment == null) {
                                repaymentRepository.insertRepayment(firestoreRepayment)
                            } else if (firestoreRepayment.updatedAt > localRepayment.updatedAt) {
                                repaymentRepository.insertRepayment(firestoreRepayment)
                            } else if (localRepayment.updatedAt > firestoreRepayment.updatedAt) {
                                syncSingleRepayment(localRepayment.id)
                            }
                        }
                    } catch (error: Exception) {
                        Log.e(TAG, "Error processing record document ${doc.id}", error)
                    }
                }

                if (validRecordIds.isNotEmpty()) {
                    // Fetch all local records matching the parsed Firestore record IDs in one go
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
                            Log.d(TAG, "Local record ${localRecord.id} is newer. Re-uploading.")
                            syncSingleRecord(localRecord.id)
                        }
                    }

                    if (recordsToInsert.isNotEmpty()) {
                        recordRepository.insertRecords(recordsToInsert)
                        Log.d(TAG, "Batch inserted ${recordsToInsert.size} records from Firestore")
                    }

                    if (recordsToUpdate.isNotEmpty()) {
                        recordRepository.updateRecords(recordsToUpdate)
                        Log.d(TAG, "Batch updated ${recordsToUpdate.size} records from Firestore")
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Error downloading records", error)
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
