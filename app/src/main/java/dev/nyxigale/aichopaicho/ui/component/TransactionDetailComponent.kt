package dev.nyxigale.aichopaicho.ui.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.*
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import dev.nyxigale.aichopaicho.ui.util.IntentUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsCard(
    recordWithRepayments: RecordWithRepayments,
    contact: Contact?,
    type: Type?,
    isEditing: Boolean,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    onNavigateToContact: (String) -> Unit
) {
    val record = recordWithRepayments.record
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timestampFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isOverdue = record.dueDate != null && record.dueDate < now && !recordWithRepayments.isSettled
    val typeName = type?.name ?: TypeConstants.getTypeName(record.typeId)

    var amountText by remember(record.amount, isEditing) {
        mutableStateOf(if (isEditing) record.amount.toString() else "")
    }
    var descriptionText by remember(record.description, isEditing) {
        mutableStateOf(if (isEditing) record.description.orEmpty() else "")
    }

    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    val currencySymbol = AppPreferenceUtils.getCurrencySymbol(context)
    val totalRepaid = recordWithRepayments.totalRepayment
    val progress = if (record.amount > 0) {
        (totalRepaid.toFloat() / record.amount.toFloat()).coerceIn(0f, 1f)
    } else if (recordWithRepayments.isSettled) {
        1f
    } else {
        0f
    }

    val heroContainerColor = MaterialTheme.colorScheme.surface
    val heroContentColor = MaterialTheme.colorScheme.onSurface
    
    val statusLabel = when {
        recordWithRepayments.isSettled -> stringResource(R.string.completed)
        isOverdue -> stringResource(R.string.overdue)
        else -> stringResource(R.string.status_open)
    }
    val statusChipColor = when {
        recordWithRepayments.isSettled -> Color(0xFF10B981)
        isOverdue -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.secondary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = heroContainerColor,
                contentColor = heroContentColor
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        text = typeName,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = recordWithRepayments.isSettled,
                            onCheckedChange = onToggleComplete,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF10B981)
                            )
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusChipColor
                        )
                    }
                }

                Column {
                    Text(
                        text = formatCurrencyAmount(
                            currency = currencySymbol,
                            amount = recordWithRepayments.remainingAmount,
                            hideAmounts = hideAmounts
                        ),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.remaining_amount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AmountSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.original_amount),
                        value = formatCurrencyAmount(currencySymbol, record.amount, hideAmounts)
                    )
                    AmountSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.total_repaid),
                        value = formatCurrencyAmount(currencySymbol, totalRepaid, hideAmounts)
                    )
                }
            }
        }

        // Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContactDisplayRow(
                    contact = contact,
                    onNavigateToContact = onNavigateToContact
                )
                
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                if (isEditing) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            onAmountChange(it)
                        },
                        label = { Text(stringResource(R.string.amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text(currencySymbol) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DateInputField(
                        label = stringResource(R.string.date),
                        selectedDate = record.date,
                        onDateSelected = { selected -> selected?.let(onDateChange) },
                        initializeWithCurrentDate = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DateInputField(
                        label = stringResource(R.string.due_date_optional),
                        selectedDate = record.dueDate,
                        onDateSelected = onDueDateChange,
                        initializeWithCurrentDate = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (record.dueDate != null) {
                        TextButton(onClick = { onDueDateChange(null) }) {
                            Text(stringResource(R.string.clear_due_date), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = {
                            descriptionText = it
                            onDescriptionChange(it)
                        },
                        label = { Text(stringResource(R.string.description_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    DetailRow(
                        label = stringResource(R.string.date),
                        value = dateFormatter.format(Date(record.date))
                    )
                    DetailRow(
                        label = stringResource(R.string.due_date),
                        value = record.dueDate?.let { due ->
                            if (isOverdue) {
                                "${stringResource(R.string.overdue)} • ${dateFormatter.format(Date(due))}"
                            } else {
                                dateFormatter.format(Date(due))
                            }
                        } ?: stringResource(R.string.none),
                        valueColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    DetailRow(
                        label = stringResource(R.string.description),
                        value = record.description ?: stringResource(R.string.no_description)
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.created_at,
                            timestampFormatter.format(Date(record.createdAt))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            R.string.updated_at,
                            timestampFormatter.format(Date(record.updatedAt))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AmountSummaryTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ContactDisplayRow(
    contact: Contact?,
    onNavigateToContact: (String) -> Unit
) {
    val context = LocalContext.current
    val primaryPhoneNumber = contact?.phone?.firstOrNull()
    val unknownLabel = stringResource(R.string.unknown)
    val contactName = contact?.name?.takeIf { it.isNotBlank() } ?: unknownLabel

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = avatarInitial(contactName),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!primaryPhoneNumber.isNullOrBlank()) {
                    Text(
                        text = primaryPhoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { contact?.id?.let { onNavigateToContact(it) } },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
        }

        if (!primaryPhoneNumber.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val opened = IntentUtils.openDialer(context = context, phoneNumber = primaryPhoneNumber)
                        if (!opened) {
                            Toast.makeText(context, R.string.unable_to_open_phone_app, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.call))
                }
                
                OutlinedButton(
                    onClick = {
                        val opened = IntentUtils.openContactDetails(context = context, contact = contact)
                        if (!opened) {
                            Toast.makeText(context, R.string.person_not_found_in_contacts, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = "System Contact")
                }
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
    val hideAmounts = rememberHideAmountsEnabled()
    val safeRemainingAmount = remainingAmount.coerceAtLeast(0)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.add_repayment),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Remaining: ${formatCurrencyAmount(AppPreferenceUtils.getCurrencySymbol(context), safeRemainingAmount, hideAmounts)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = repaymentAmount,
            onValueChange = onRepaymentAmountChange,
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text(AppPreferenceUtils.getCurrencySymbol(context)) },
            isError = repaymentAmount.toIntOrNull() ?: 0 > safeRemainingAmount && safeRemainingAmount > 0,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = repaymentDescription,
            onValueChange = onRepaymentDescriptionChange,
            label = { Text(stringResource(R.string.description_optional)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onSaveRepayment,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && (repaymentAmount.toIntOrNull() ?: 0 > 0) && (repaymentAmount.toIntOrNull() ?: 0 <= safeRemainingAmount),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.save_repayment), fontWeight = FontWeight.Bold)
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

    val sortedRepayments = remember(repayments) { repayments.sortedByDescending { it.date } }
    var showAll by remember(repayments) { mutableStateOf(false) }
    val visibleRepayments = if (showAll) sortedRepayments else sortedRepayments.take(3)
    val canExpand = sortedRepayments.size > 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.repayment_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (canExpand) {
                    TextButton(onClick = { showAll = !showAll }) {
                        Text(if (showAll) stringResource(R.string.show_less) else stringResource(R.string.see_all))
                    }
                }
            }
            
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            visibleRepayments.forEachIndexed { index, repayment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatCurrencyAmount(AppPreferenceUtils.getCurrencySymbol(context), repayment.amount, hideAmounts),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!repayment.description.isNullOrBlank()) {
                            Text(
                                text = repayment.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = dateFormatter.format(Date(repayment.date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index != visibleRepayments.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

private fun avatarInitial(name: String): String {
    val initial = name.firstOrNull { it.isLetterOrDigit() } ?: '?'
    return initial.uppercaseChar().toString()
}

@Preview(showBackground = true, name = "TransactionDetailsCard - View Mode")
@Composable
fun TransactionDetailsCardPreview() {
    AichoPaichoTheme {
        val sampleRecord = Record(
            id = "1", userId = "user1", contactId = "contact1", typeId = TypeConstants.LENT_ID,
            amount = 12550,
            date = System.currentTimeMillis() - 86400000L * 5,
            dueDate = System.currentTimeMillis() + 86400000L * 3,
            description = "Lunch with client. Discussed project milestones and future collaboration.",
            isComplete = false,
            createdAt = System.currentTimeMillis() - 86400000L * 10,
            updatedAt = System.currentTimeMillis() - 86400000L * 2,
            isDeleted = false
        )
        val sampleRecordWithRepayments = RecordWithRepayments(sampleRecord, emptyList())
        val sampleContact = Contact(id = "c1", name = "Alex Johnson", phone = listOf("555-0101"), contactId = "101", userId = "u1")
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
            onToggleComplete = {},
            onNavigateToContact = {}
        )
    }
}
