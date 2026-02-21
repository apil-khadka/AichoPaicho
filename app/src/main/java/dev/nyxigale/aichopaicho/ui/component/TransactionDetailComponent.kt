package dev.nyxigale.aichopaicho.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Repayment
import dev.nyxigale.aichopaicho.data.entity.Type
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsCard(
    recordWithRepayments: RecordWithRepayments, // Changed parameter
    contact: Contact?,
    type: Type?,
    isEditing: Boolean,
    onAmountChange: (String) -> Unit, // Changed to String
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit, // Kept, though no UI for edit in this card
    onDueDateChange: (Long?) -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    // onCompletionToggle: () -> Unit // Removed, as completion is now derived
) {
    val record = recordWithRepayments.record // Unpack for convenience
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isRecurring = !record.recurringTemplateId.isNullOrBlank()
    val isOverdue = record.dueDate != null && record.dueDate < now && !recordWithRepayments.isSettled
    // Update local state handling for editing
    var amountText by remember(record.amount, isEditing) {
        // Initialize with formatted string if editing, otherwise it's not shown in an input field
        mutableStateOf(if (isEditing) record.amount.toString() else "")
    }
    var descriptionText by remember(record.description, isEditing) {
        mutableStateOf(if (isEditing) record.description ?: "" else "")
    }

    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.transaction_details),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (isRecurring) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.recurring),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = recordWithRepayments.isSettled,
                        onCheckedChange = { checked -> onToggleComplete(checked) }
                    )
                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ContactDisplayRow(contact)

            DetailRow(
                label = stringResource(R.string.type),
                value = (type?.name
                    ?: TypeConstants.getTypeName(record.typeId)), // Use typeId as fallback
                isEditing = false
            )

            // Original Amount Display
            DetailRow(
                label = "Original Amount", // New label
                value = formatCurrencyAmount(
                    currency = AppPreferenceUtils.getCurrencySymbol(context),
                    amount = record.amount,
                    hideAmounts = hideAmounts
                ),
                isEditing = false
            )

            // Total Repaid Display (if any)
            if (recordWithRepayments.totalRepayment > 0) {
                DetailRow(
                    label = "Total Repaid", // New label
                    value = formatCurrencyAmount(
                        currency = AppPreferenceUtils.getCurrencySymbol(context),
                        amount = recordWithRepayments.totalRepayment,
                        hideAmounts = hideAmounts
                    ),
                    isEditing = false
                )
            }

            // Remaining Amount Display
            if (isEditing) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it // Update local state
                        onAmountChange(it) // Pass raw string to ViewModel for parsing
                    },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text(AppPreferenceUtils.getCurrencySymbol(context)) }
                )
            } else {
                DetailRow(
                    label = "Remaining Amount", // New label
                    value = formatCurrencyAmount(
                        currency = AppPreferenceUtils.getCurrencySymbol(context),
                        amount = recordWithRepayments.remainingAmount,
                        hideAmounts = hideAmounts
                    ),
                    isEditing = false
                )
            }


            DetailRow(
                label = stringResource(R.string.date),
                value = dateFormatter.format(Date(record.date)),
                isEditing = false // Date is not editable in this component
            )

            if (isEditing) {
                DateInputField(
                    label = stringResource(R.string.due_date_optional),
                    selectedDate = record.dueDate,
                    onDateSelected = onDueDateChange,
                    initializeWithCurrentDate = false,
                    modifier = Modifier.fillMaxWidth()
                )
                if (record.dueDate != null) {
                    TextButton(onClick = { onDueDateChange(null) }) {
                        Text(stringResource(R.string.clear_due_date))
                    }
                }
            } else {
                DetailRow(
                    label = stringResource(R.string.due_date),
                    value = record.dueDate?.let { dateFormatter.format(Date(it)) }
                        ?: stringResource(R.string.none),
                    valueColor = if (isOverdue) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    isEditing = false
                )
            }

            if (isEditing) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it // Update local state
                        onDescriptionChange(it)
                    },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            } else {
                DetailRow(
                    label = stringResource(R.string.description),
                    value = record.description ?: stringResource(R.string.no_description),
                    isEditing = false
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.created_at,
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(
                            Date(record.createdAt)
                        )
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.updated_at,
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(
                            Date(record.updatedAt)
                        )
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isEditing: Boolean // Parameter kept for potential future use or consistency
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
fun ContactDisplayRow(
    contact: Contact?
) {
    val context = LocalContext.current
    val primaryPhoneNumber = contact?.phone?.firstOrNull() // Get the primary phone number

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) { // Allow name to take available space
            DetailRow(
                label = stringResource(R.string.contact),
                value = contact?.name ?: "Unknown Contact",
                isEditing = false,
            )
        }

        if (!primaryPhoneNumber.isNullOrBlank()) { // Only show button if a phone number exists
            Button(
                onClick = {
                    // Call the portable function from ContactAccessComponent.kt
                    openContactDetailsByPhoneNumber(context = context, phoneNumber = primaryPhoneNumber)
                },
                modifier = Modifier.padding(start = 8.dp) // Add some spacing
            ) {
                Text(text = stringResource(R.string.view_details))
            }
        }
    }
}

@Composable
fun AddRepaymentCard(
    repaymentAmount: String,
    onRepaymentAmountChange: (String) -> Unit,
    repaymentDescription: String,
    onRepaymentDescriptionChange: (String) -> Unit,
    onSaveRepayment: () -> Unit,
    isLoading: Boolean,
    remainingAmount: Int
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Add Repayment",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = repaymentAmount,
                onValueChange = onRepaymentAmountChange,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text(AppPreferenceUtils.getCurrencySymbol(context)) },
                isError = repaymentAmount.toIntOrNull() ?: 0 > remainingAmount && remainingAmount > 0
            )
            if (repaymentAmount.toIntOrNull() ?: 0 > remainingAmount && remainingAmount > 0) {
                Text(
                    text = "Repayment amount cannot exceed remaining amount.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = repaymentDescription,
                onValueChange = onRepaymentDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Button(
                onClick = onSaveRepayment,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && (repaymentAmount.toIntOrNull() ?: 0 > 0) && (repaymentAmount.toIntOrNull() ?: 0 <= remainingAmount)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Repayment")
                }
            }
        }
    }
}

@Composable
fun RepaymentHistoryCard(
    repayments: List<Repayment>
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()

    if (repayments.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Repayment History",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            repayments.forEach { repayment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatCurrencyAmount(
                                currency = AppPreferenceUtils.getCurrencySymbol(context),
                                amount = repayment.amount,
                                hideAmounts = hideAmounts
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        repayment.description?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = dateFormatter.format(Date(repayment.date)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (repayment != repayments.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "TransactionDetailsCard - View Mode")
@Composable
fun TransactionDetailsCardPreview() {
    AichoPaichoTheme {
        val sampleRecord = Record(
            id = "1", userId = "user1", contactId = "contact1", typeId = TypeConstants.LENT_ID,
            amount = 12550, // e.g. 125.50
            date = System.currentTimeMillis() - 86400000L * 5, // 5 days ago
            dueDate = System.currentTimeMillis() + 86400000L * 3, // 3 days from now
            description = "Lunch with client. Discussed project milestones and future collaboration opportunities.",
            isComplete = false,
            createdAt = System.currentTimeMillis() - 86400000L * 10, // 10 days ago
            updatedAt = System.currentTimeMillis() - 86400000L * 2,  // 2 days ago
            isDeleted = false
        )
        val sampleRecordWithRepayments = RecordWithRepayments(sampleRecord, emptyList())
        val sampleContact = Contact(
            id = "contact1",
            name = "Alex Johnson",
            phone = listOf("555-0101"),
            contactId = "101",
            userId = "user1"
        )
        val sampleType = Type(id = TypeConstants.LENT_ID, name = "Lent")

        TransactionDetailsCard(
            recordWithRepayments = sampleRecordWithRepayments,
            contact = sampleContact,
            type = sampleType,
            isEditing = false,
            onAmountChange = {},
            onDescriptionChange = {},
            onDateChange = {},
            onDueDateChange = {},
            onToggleComplete = {}
        )
    }
}

@Preview(showBackground = true, name = "TransactionDetailsCard - Edit Mode")
@Composable
fun TransactionDetailsCardEditingPreview() {
    AichoPaichoTheme {
        val sampleRecord = Record(
            id = "2", userId = "user1", contactId = "contact2", typeId = TypeConstants.BORROWED_ID,
            amount = 7500, // e.g. 75.00
            date = System.currentTimeMillis() - 86400000L * 3, // 3 days ago
            dueDate = System.currentTimeMillis() - 86400000L, // 1 day ago
            description = "Shared expenses for team outing.",
            isComplete = true,
            createdAt = System.currentTimeMillis() - 86400000L * 7, // 7 days ago
            updatedAt = System.currentTimeMillis() - 86400000L * 1,  // 1 day ago
            isDeleted = false
        )
        val sampleRepayment = Repayment(recordId = "2", amount = 2500, date = System.currentTimeMillis() - 86400000L, description = "Partial repayment")
        val sampleRecordWithRepayments = RecordWithRepayments(sampleRecord, listOf(sampleRepayment))
        val sampleContact = Contact(
            id = "contact2",
            name = "Maria Garcia",
            phone = listOf("555-0202"),
            contactId = "102",
            userId = "user1"
        )
        val sampleType = Type(id = TypeConstants.BORROWED_ID, name = "Borrowed")

        TransactionDetailsCard(
            recordWithRepayments = sampleRecordWithRepayments,
            contact = sampleContact,
            type = sampleType,
            isEditing = true,
            onAmountChange = {},
            onDescriptionChange = {},
            onDateChange = {},
            onDueDateChange = {},
            onToggleComplete = {}
        )
    }
}
