package dev.nyxigale.aichopaicho.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    recordWithRepayments: RecordWithRepayments,
    contact: Contact?,
    type: Type?,
    isEditing: Boolean,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onToggleComplete: (Boolean) -> Unit
) {
    val record = recordWithRepayments.record
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
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

    val heroContainerColor = Color(0xFFF0F2F5)
    val heroContentColor = MaterialTheme.colorScheme.onSurface
    val statusLabel = when {
        recordWithRepayments.isSettled -> stringResource(R.string.completed)
        isOverdue -> stringResource(R.string.overdue)
        else -> stringResource(R.string.status_open)
    }
    val statusChipColor = when {
        recordWithRepayments.isSettled -> Color(0xFF2E9C5E)
        isOverdue -> Color(0xFFD07A44)
        else -> Color(0xFF7A879A)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = heroContainerColor,
                contentColor = heroContentColor
            )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(
                            text = typeName,
                            containerColor = Color(0x1FB94F83),
                            contentColor = Color(0xFF8F3467)
                        )
                        StatusChip(
                            text = statusLabel,
                            containerColor = statusChipColor,
                            contentColor = Color.White
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = recordWithRepayments.isSettled,
                            onCheckedChange = onToggleComplete,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFB94F83),
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = stringResource(R.string.completed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = formatCurrencyAmount(
                        currency = currencySymbol,
                        amount = recordWithRepayments.remainingAmount,
                        hideAmounts = hideAmounts
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.remaining_amount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFB94F83),
                    trackColor = Color(0xFFDCE2EA)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmountSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.original_amount),
                        value = formatCurrencyAmount(currencySymbol, record.amount, hideAmounts),
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                    AmountSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.total_repaid),
                        value = formatCurrencyAmount(currencySymbol, totalRepaid, hideAmounts),
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                    AmountSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.remaining_amount),
                        value = formatCurrencyAmount(
                            currencySymbol,
                            recordWithRepayments.remainingAmount,
                            hideAmounts
                        ),
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactDisplayRow(contact = contact)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                DetailRow(label = stringResource(R.string.type), value = typeName)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

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
                        prefix = { Text(currencySymbol) }
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
                            Text(stringResource(R.string.clear_due_date))
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
                        maxLines = 5
                    )
                } else {
                    DetailRow(
                        label = stringResource(R.string.date),
                        value = dateFormatter.format(Date(record.date))
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    DetailRow(
                        label = stringResource(R.string.due_date),
                        value = record.dueDate?.let { due ->
                            if (isOverdue) {
                                "${stringResource(R.string.overdue)} | ${dateFormatter.format(Date(due))}"
                            } else {
                                dateFormatter.format(Date(due))
                            }
                        } ?: stringResource(R.string.none),
                        valueColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    DetailRow(
                        label = stringResource(R.string.description),
                        value = record.description ?: stringResource(R.string.no_description)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
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
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AmountSummaryTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.65f)
        )
    }
}

@Composable
fun ContactDisplayRow(
    contact: Contact?
) {
    val context = LocalContext.current
    val primaryPhoneNumber = contact?.phone?.firstOrNull()
    val unknownLabel = stringResource(R.string.unknown)
    val contactName = contact?.name?.takeIf { it.isNotBlank() } ?: unknownLabel

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.contact),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.35f)
            )
            Row(
                modifier = Modifier.weight(0.65f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = Color(0x1FFF5EA1)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = avatarInitial(contactName),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB94F83)
                        )
                    }
                }
                Text(
                    text = contactName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (!primaryPhoneNumber.isNullOrBlank()) {
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val opened = openDialer(context = context, phoneNumber = primaryPhoneNumber)
                        if (!opened) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.unable_to_open_phone_app),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1E5EE),
                        contentColor = Color(0xFF7F285A)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = stringResource(R.string.call),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = {
                        val opened = openContactDetails(context = context, contact = contact)
                        if (!opened) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.person_not_found_in_contacts),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD45994),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_details),
                        style = MaterialTheme.typography.labelLarge
                    )
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.add_repayment),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${stringResource(R.string.remaining_amount)}: ${
                    formatCurrencyAmount(
                        currency = AppPreferenceUtils.getCurrencySymbol(context),
                        amount = safeRemainingAmount,
                        hideAmounts = hideAmounts
                    )
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            OutlinedTextField(
                value = repaymentAmount,
                onValueChange = onRepaymentAmountChange,
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text(AppPreferenceUtils.getCurrencySymbol(context)) },
                isError = repaymentAmount.toIntOrNull() ?: 0 > safeRemainingAmount && safeRemainingAmount > 0
            )
            if (repaymentAmount.toIntOrNull() ?: 0 > safeRemainingAmount && safeRemainingAmount > 0) {
                Text(
                    text = stringResource(R.string.repayment_amount_exceeds_remaining),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            OutlinedTextField(
                value = repaymentDescription,
                onValueChange = onRepaymentDescriptionChange,
                label = { Text(stringResource(R.string.description_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Button(
                onClick = onSaveRepayment,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading &&
                    (repaymentAmount.toIntOrNull() ?: 0 > 0) &&
                    (repaymentAmount.toIntOrNull() ?: 0 <= safeRemainingAmount),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD45994),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.save_repayment))
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

    val sortedRepayments = remember(repayments) { repayments.sortedByDescending { it.date } }
    var showAll by remember(repayments) { mutableStateOf(false) }
    val visibleRepayments = if (showAll) sortedRepayments else sortedRepayments.take(3)
    val canExpand = sortedRepayments.size > 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.repayment_history),
                    style = MaterialTheme.typography.titleLarge
                )
                if (canExpand) {
                    TextButton(onClick = { showAll = !showAll }) {
                        Text(
                            text = if (showAll) {
                                stringResource(R.string.show_less)
                            } else {
                                stringResource(R.string.see_all)
                            }
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            visibleRepayments.forEachIndexed { index, repayment ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF3F4F7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0x1FFF5EA1)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "R",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB94F83)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = formatCurrencyAmount(
                                    currency = AppPreferenceUtils.getCurrencySymbol(context),
                                    amount = repayment.amount,
                                    hideAmounts = hideAmounts
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F9D57)
                            )
                            repayment.description?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = dateFormatter.format(Date(repayment.date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
                if (index != visibleRepayments.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
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
            description = "Lunch with client. Discussed project milestones and future collaboration opportunities.",
            isComplete = false,
            createdAt = System.currentTimeMillis() - 86400000L * 10,
            updatedAt = System.currentTimeMillis() - 86400000L * 2,
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
            amount = 7500,
            date = System.currentTimeMillis() - 86400000L * 3,
            dueDate = System.currentTimeMillis() - 86400000L,
            description = "Shared expenses for team outing.",
            isComplete = true,
            createdAt = System.currentTimeMillis() - 86400000L * 7,
            updatedAt = System.currentTimeMillis() - 86400000L * 1,
            isDeleted = false
        )
        val sampleRepayment = Repayment(
            recordId = "2",
            amount = 2500,
            date = System.currentTimeMillis() - 86400000L,
            description = "Partial repayment"
        )
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
