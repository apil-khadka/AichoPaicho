package dev.nyxigale.aichopaicho.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.Repayment
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvTransferResult(
    val success: Boolean,
    val message: String,
    val location: String? = null
)

@Singleton
class CsvTransferService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val recordRepository: RecordRepository,
    private val repaymentRepository: RepaymentRepository
) {

    suspend fun exportCsvBundleToFolder(folderUri: Uri): CsvTransferResult = withContext(Dispatchers.IO) {
        runCatching {
            val targetRoot = DocumentFile.fromTreeUri(context, folderUri)
                ?: return@runCatching CsvTransferResult(false, "Unable to open selected folder")
            if (!targetRoot.canWrite()) {
                return@runCatching CsvTransferResult(false, "Cannot write to selected folder")
            }

            val contacts = contactRepository.getAllContacts().first()
            val records = recordRepository.getAllRecords().first()
            val repayments = repaymentRepository.getAllRepayments()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportFolderName = "aichopaicho_csv_$timestamp"
            val exportFolder = targetRoot.createDirectory(exportFolderName)
                ?: return@runCatching CsvTransferResult(false, "Failed to create export folder")

            writeCsvDocument(
                parent = exportFolder,
                fileName = "contacts.csv",
                header = listOf("id", "name", "userId", "phone", "contactId", "isDeleted", "createdAt", "updatedAt"),
                rows = contacts.map { contact ->
                    listOf(
                        contact.id,
                        contact.name,
                        contact.userId.orEmpty(),
                        contact.phone.joinToString("|") { it.orEmpty() },
                        contact.contactId.orEmpty(),
                        contact.isDeleted.toString(),
                        contact.createdAt.toString(),
                        contact.updatedAt.toString()
                    )
                }
            )

            writeCsvDocument(
                parent = exportFolder,
                fileName = "records.csv",
                header = listOf(
                    "id",
                    "userId",
                    "contactId",
                    "typeId",
                    "amount",
                    "date",
                    "dueDate",
                    "isComplete",
                    "isDeleted",
                    "description",
                    "recurringTemplateId",
                    "createdAt",
                    "updatedAt"
                ),
                rows = records.map { record ->
                    listOf(
                        record.id,
                        record.userId.orEmpty(),
                        record.contactId.orEmpty(),
                        record.typeId.toString(),
                        record.amount.toString(),
                        record.date.toString(),
                        record.dueDate?.toString().orEmpty(),
                        record.isComplete.toString(),
                        record.isDeleted.toString(),
                        record.description.orEmpty(),
                        record.recurringTemplateId.orEmpty(),
                        record.createdAt.toString(),
                        record.updatedAt.toString()
                    )
                }
            )

            writeCsvDocument(
                parent = exportFolder,
                fileName = "repayments.csv",
                header = listOf("id", "recordId", "amount", "date", "description", "createdAt", "updatedAt"),
                rows = repayments.map { repayment ->
                    listOf(
                        repayment.id,
                        repayment.recordId,
                        repayment.amount.toString(),
                        repayment.date.toString(),
                        repayment.description.orEmpty(),
                        repayment.createdAt.toString(),
                        repayment.updatedAt.toString()
                    )
                }
            )

            CsvTransferResult(
                success = true,
                message = "CSV export completed",
                location = exportFolder.uri.toString()
            )
        }.getOrElse { error ->
            CsvTransferResult(
                success = false,
                message = "CSV export failed: ${error.message}"
            )
        }
    }

    suspend fun importFromCsvFile(fileUri: Uri): CsvTransferResult = withContext(Dispatchers.IO) {
        runCatching {
            val rows = readCsvFromUri(fileUri)
            if (rows.isEmpty()) {
                return@runCatching CsvTransferResult(false, "CSV file is empty")
            }

            val header = rows.first().map { it.trim() }
            val body = rows.drop(1)

            val resultMessage = when {
                isContactsHeader(header) -> {
                    val imported = importContactsRows(body)
                    "Imported $imported contacts"
                }

                isRecordsHeader(header) -> {
                    val imported = importRecordsRows(body)
                    "Imported $imported records"
                }

                isRepaymentsHeader(header) -> {
                    val imported = importRepaymentsRows(body)
                    "Imported $imported repayments"
                }

                else -> {
                    return@runCatching CsvTransferResult(
                        success = false,
                        message = "Unrecognized CSV format. Use contacts.csv, records.csv, or repayments.csv"
                    )
                }
            }

            CsvTransferResult(
                success = true,
                message = resultMessage,
                location = fileUri.toString()
            )
        }.getOrElse { error ->
            CsvTransferResult(
                success = false,
                message = "CSV import failed: ${error.message}"
            )
        }
    }

    private suspend fun importContactsRows(rows: List<List<String>>): Int {
        var imported = 0
        rows.forEach { columns ->
            if (columns.size < 8) return@forEach
            val id = columns[0]
            if (id.isBlank()) return@forEach

            val contact = Contact(
                id = id,
                name = columns[1],
                userId = columns[2].ifBlank { null },
                phone = columns[3].split('|').map { value -> value.ifBlank { null } },
                contactId = columns[4].ifBlank { null },
                isDeleted = columns[5].toBooleanStrictOrNull() ?: false,
                createdAt = columns[6].toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = columns[7].toLongOrNull() ?: System.currentTimeMillis()
            )
            contactRepository.insertContact(contact)
            imported++
        }
        return imported
    }

    private suspend fun importRecordsRows(rows: List<List<String>>): Int {
        var imported = 0
        rows.forEach { columns ->
            if (columns.size < 12) return@forEach
            val id = columns[0]
            if (id.isBlank()) return@forEach

            val hasRecurringTemplateIdColumn = columns.size >= 13
            val recurringTemplateId = if (hasRecurringTemplateIdColumn) {
                columns[10].ifBlank { null }
            } else {
                null
            }
            val createdAtIndex = if (hasRecurringTemplateIdColumn) 11 else 10
            val updatedAtIndex = if (hasRecurringTemplateIdColumn) 12 else 11

            val record = Record(
                id = id,
                userId = columns[1].ifBlank { null },
                contactId = columns[2].ifBlank { null },
                typeId = columns[3].toIntOrNull() ?: 0,
                amount = columns[4].toIntOrNull() ?: 0,
                date = columns[5].toLongOrNull() ?: System.currentTimeMillis(),
                dueDate = columns[6].toLongOrNull(),
                isComplete = columns[7].toBooleanStrictOrNull() ?: false,
                isDeleted = columns[8].toBooleanStrictOrNull() ?: false,
                description = columns[9].ifBlank { null },
                recurringTemplateId = recurringTemplateId,
                createdAt = columns[createdAtIndex].toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = columns[updatedAtIndex].toLongOrNull() ?: System.currentTimeMillis()
            )
            recordRepository.upsert(record)
            imported++
        }
        return imported
    }

    private suspend fun importRepaymentsRows(rows: List<List<String>>): Int {
        var imported = 0
        rows.forEach { columns ->
            if (columns.size < 7) return@forEach
            val id = columns[0]
            if (id.isBlank()) return@forEach

            val repayment = Repayment(
                id = id,
                recordId = columns[1],
                amount = columns[2].toIntOrNull() ?: 0,
                date = columns[3].toLongOrNull() ?: System.currentTimeMillis(),
                description = columns[4].ifBlank { null },
                createdAt = columns[5].toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = columns[6].toLongOrNull() ?: System.currentTimeMillis()
            )
            repaymentRepository.insertRepayment(repayment)
            imported++
        }
        return imported
    }

    private fun writeCsvDocument(
        parent: DocumentFile,
        fileName: String,
        header: List<String>,
        rows: List<List<String>>
    ) {
        parent.findFile(fileName)?.delete()
        val file = parent.createFile("text/csv", fileName)
            ?: throw IllegalStateException("Failed to create $fileName")

        val outputStream = context.contentResolver.openOutputStream(file.uri, "w")
            ?: throw IllegalStateException("Failed to open output stream for $fileName")

        OutputStreamWriter(outputStream).use { writer ->
            writer.appendLine(header.joinToString(","))
            rows.forEach { row ->
                writer.appendLine(row.joinToString(",") { value -> escapeCsv(value) })
            }
            writer.flush()
        }
    }

    private fun readCsvFromUri(uri: Uri): List<List<String>> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open selected file")

        return inputStream.bufferedReader().useLines { sequence ->
            sequence
                .filter { it.isNotBlank() }
                .map { line -> parseCsvLine(line) }
                .toList()
        }
    }

    private fun isContactsHeader(header: List<String>): Boolean {
        return header.containsAll(listOf("id", "name", "phone", "contactId"))
    }

    private fun isRecordsHeader(header: List<String>): Boolean {
        return header.containsAll(listOf("id", "typeId", "amount", "date", "isComplete"))
    }

    private fun isRepaymentsHeader(header: List<String>): Boolean {
        return header.containsAll(listOf("id", "recordId", "amount", "date"))
    }

    private fun escapeCsv(raw: String): String {
        var safe = raw
        if (safe.isNotEmpty()) {
            val firstChar = safe[0]
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
                safe = "'" + safe
            }
        }
        val shouldQuote = safe.contains(',') || safe.contains('"') || safe.contains('\n')
        if (!shouldQuote) return safe
        return "\"" + safe.replace("\"", "\"\"") + "\""
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '"' -> {
                    if (inQuotes && index + 1 < line.length && line[index + 1] == '"') {
                        current.append('"')
                        index++
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                ch == ',' && !inQuotes -> {
                    values.add(current.toString())
                    current.clear()
                }

                else -> current.append(ch)
            }
            index++
        }
        values.add(current.toString())
        return values
    }
}
