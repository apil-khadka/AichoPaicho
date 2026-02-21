package dev.nyxigale.aichopaicho.ui.component

// import androidx.compose.ui.res.stringResource // Not used
// import androidx.compose.ui.unit.sp // Replaced with MaterialTheme.typography
// import dev.nyxigale.aichopaicho.R // Not used
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.entity.Record
import dev.nyxigale.aichopaicho.data.entity.RecordWithRepayments
import dev.nyxigale.aichopaicho.data.entity.Type
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.ui.util.formatAbsoluteCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.formatSignedCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactSummaryCard(
    contact: Contact?,
    totalLent: Double,
    totalBorrowed: Double,
    netBalance: Double,
    showCompleted: Boolean,
    onShowCompletedChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Added spacing
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer), // Themed
                contentAlignment = Alignment.Center
            ) {
                if (contact?.name?.isNotEmpty() == true) {
                    Text(
                        text = contact.name.first().uppercaseChar().toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer, // Themed
                        style = MaterialTheme.typography.headlineLarge // Themed
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Contact Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, // Themed
                        modifier = Modifier.size(48.dp) // Adjusted size
                    )
                }
            }

            // Net Balance
            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.labelMedium, // Themed
                color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
            )

            val netBalanceColor = when {
                netBalance > 0 -> MaterialTheme.colorScheme.primary // User lent more (contact owes user)
                netBalance < 0 -> MaterialTheme.colorScheme.error   // User borrowed more (user owes contact)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = formatAbsoluteCurrencyAmount(
                    currency = AppPreferenceUtils.getCurrencySymbol(context),
                    amount = netBalance,
                    hideAmounts = hideAmounts
                ),
                style = MaterialTheme.typography.headlineMedium, // Themed
                color = netBalanceColor
            )

            Text(
                text = when {
                    netBalance > 0 -> stringResource(R.string.they_owe_you)
                    netBalance < 0 -> stringResource(R.string.you_owe_them)
                    else -> stringResource(R.string.all_settled)
                },
                style = MaterialTheme.typography.bodySmall, // Themed
                color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
            )

            // Lent and Borrowed Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = stringResource(R.string.total_lent), // Clarified label
                    amount = totalLent,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    color = MaterialTheme.colorScheme.primary // Use theme color
                )
                SummaryItem(
                    label = stringResource(R.string.total_borrowed), // Clarified label
                    amount = totalBorrowed,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    color = MaterialTheme.colorScheme.error // Use theme color
                )
            }

            // Show Completed Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onShowCompletedChanged(!showCompleted) } // Make row clickable
            ) {
                Checkbox(
                    checked = showCompleted,
                    onCheckedChange = onShowCompletedChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.show_completed_transactions),
                    style = MaterialTheme.typography.bodyMedium // Themed
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp) // Slightly larger
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCurrencyAmount(
                    currency = AppPreferenceUtils.getCurrencySymbol(context),
                    amount = amount,
                    hideAmounts = hideAmounts
                ),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), // Themed
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall, // Themed
            color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
        )
    }
}

@Composable
fun ContactRecordTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    allCount: Int,
    lentCount: Int,
    borrowedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Consistent shape
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer, // Themed
            contentColor = MaterialTheme.colorScheme.onSurface // Themed
        )
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            TabButtonInternal(
                text = stringResource(R.string.all_count, allCount),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButtonInternal(
                text = stringResource(R.string.lent_count, lentCount),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            TabButtonInternal(
                text = stringResource(R.string.borrowed_count, borrowedCount),
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButtonInternal( // Renamed to avoid conflict if a general TabButton is introduced
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 2.dp, vertical = 4.dp), // Adjusted padding
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp) // Consistent shape
    ) {
        Text(
            text = text,
            style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium, // Themed
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EmptyRecordsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow, // Themed
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant // Themed
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.no_records), // Title case
                style = MaterialTheme.typography.titleMedium, // Themed
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.no_transactions), // More descriptive
                style = MaterialTheme.typography.bodyMedium, // Themed
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ContactRecordCard(
    recordWithRepayments: RecordWithRepayments,
    type: Type?,
    onRecordClick: () -> Unit,
    onDeleteRecord: () -> Unit,
    onToggleComplete: (Boolean) -> Unit
) {
    val record = recordWithRepayments.record // Unpack for convenience
    val dateFormatter = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }
    val dueFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm a", Locale.getDefault()) }
    val isLent = record.typeId == TypeConstants.LENT_ID
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    val now = System.currentTimeMillis()
    val isOverdue = record.dueDate != null && record.dueDate < now && !recordWithRepayments.isSettled

    val amountColor = if (isLent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val typeName = type?.name ?: if(isLent) "Lent" else "Borrowed"
    val typeContainerColor = if (isLent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val typeContentColor = if (isLent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val amountPrefix = if (isLent) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecordClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box( // Type indicator
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(amountColor)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatSignedCurrencyAmount(
                            sign = amountPrefix,
                            currency = AppPreferenceUtils.getCurrencySymbol(context),
                            amount = recordWithRepayments.remainingAmount,
                            hideAmounts = hideAmounts
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = amountColor,
                        textDecoration = if (recordWithRepayments.isSettled) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = typeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeContentColor,
                        modifier = Modifier
                            .background(
                                color = typeContainerColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Show original amount if it's different from remaining and not settled
                if (recordWithRepayments.totalRepayment > 0 && !recordWithRepayments.isSettled) {
                    Text(
                        text = "Original: ${
                            formatCurrencyAmount(
                                currency = AppPreferenceUtils.getCurrencySymbol(context),
                                amount = record.amount,
                                hideAmounts = hideAmounts
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Text(
                    text = "${dateFormatter.format(Date(record.date))} at ${timeFormatter.format(Date(record.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                record.dueDate?.let { due ->
                    Text(
                        text = if (isOverdue) {
                            "${stringResource(R.string.overdue)} ${dueFormatter.format(Date(due))}"
                        } else {
                            "${stringResource(R.string.due)} ${dueFormatter.format(Date(due))}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }

                record.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (recordWithRepayments.isSettled) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Checkbox(
                    checked = recordWithRepayments.isSettled,
                    onCheckedChange = { checked -> onToggleComplete(checked) },
                    modifier = Modifier.size(36.dp)
                )
                IconButton(
                    onClick = onDeleteRecord,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactHeadingDisplay(
    contact: Contact?
) {
    val context = LocalContext.current
    val primaryPhoneNumber = contact?.phone?.firstOrNull()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = contact?.name ?: stringResource(R.string.contact_details), // Fallback title
            style = MaterialTheme.typography.titleLarge, // Themed for TopAppBar
            // Color will be inherited from TopAppBar
            modifier = Modifier.weight(1f, fill = false) // Allow text to take space but not push button
        )
        if (!primaryPhoneNumber.isNullOrBlank()) {
            TextButton(
                onClick = {
                    openContactDetailsByPhoneNumber(context = context, phoneNumber = primaryPhoneNumber)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_details),
                    color = MaterialTheme.colorScheme.primary // Themed
                )
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun ContactSummaryCardPreview() {
    AichoPaichoTheme {
        ContactSummaryCard(
            contact = Contact(id = "c1", name = "John Doe", phone = emptyList(), contactId = "123", userId="u1"),
            totalLent = 150.75,
            totalBorrowed = 50.25,
            netBalance = 100.50, // They owe you
            showCompleted = false,
            onShowCompletedChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactSummaryCardPreview_OweThem() {
    AichoPaichoTheme {
        ContactSummaryCard(
            contact = Contact(id = "c1", name = "Jane Smith", phone = emptyList(), contactId = "124", userId="u1"),
            totalLent = 50.0,
            totalBorrowed = 200.0,
            netBalance = -150.0, // You owe them
            showCompleted = true,
            onShowCompletedChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactRecordTabsPreview() {
    AichoPaichoTheme {
        ContactRecordTabs(
            selectedTab = 0,
            onTabSelected = {},
            allCount = 10,
            lentCount = 5,
            borrowedCount = 5
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyRecordsCardPreview() {
    AichoPaichoTheme {
        EmptyRecordsCard()
    }
}

@Preview(showBackground = true)
@Composable
fun ContactRecordCardLentPreview() {
    AichoPaichoTheme {
        val record = Record(
            id = "1",
            userId = "u1",
            contactId = "c1",
            typeId = TypeConstants.LENT_ID,
            amount = 12000,
            date = System.currentTimeMillis(),
            dueDate = null,
            isComplete = false,
            isDeleted = false,
            description = "Lunch",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val type = Type(TypeConstants.LENT_ID, "Lent")
        ContactRecordCard(
            recordWithRepayments = RecordWithRepayments(record, emptyList()),
            type = type,
            onRecordClick = {},
            onDeleteRecord = {},
            onToggleComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactRecordCardBorrowedPreview() {
    AichoPaichoTheme {
        val record = Record(
            id = "2",
            userId = "u1",
            contactId = "c1",
            typeId = TypeConstants.BORROWED_ID,
            amount = 7550,
            date = System.currentTimeMillis() - 86400000,
            dueDate = null,
            isComplete = false,
            isDeleted = true,
            description = "just for fun",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val type = Type(TypeConstants.BORROWED_ID, "Borrowed")
        ContactRecordCard(
            recordWithRepayments = RecordWithRepayments(record, emptyList()),
            type = type,
            onRecordClick = {},
            onDeleteRecord = {},
            onToggleComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactHeadingDisplayPreview() {
    AichoPaichoTheme {
        Surface(color = MaterialTheme.colorScheme.surface) { // Simulate TopAppBar background
            ContactHeadingDisplay(contact = Contact("c1", "Alice Wonderland", null, emptyList(), "u1"))
        }
    }
}
