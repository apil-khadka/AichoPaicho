package com.aspiring_creators.aichopaicho.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aspiring_creators.aichopaicho.AppPreferenceUtils
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.data.entity.Record
import com.aspiring_creators.aichopaicho.data.entity.RecordWithRepayments
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added for preview
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactPreview // Updated import
import java.text.SimpleDateFormat
import java.util.*
import kotlin.String
import kotlin.Unit
import kotlin.to


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTopBar(
    onNavigateBack: () -> Unit,
    dateRange: Pair<Long, Long>, // Represents actual current filter (can be MIN/MAX_VALUE)
    onDateRangeSelected: (Long, Long) -> Unit,
    onContactsNavigation: () -> Unit // New callback for contacts
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }

    // Display text: Show "All Time" or specific dates
    val startDateText = if (dateRange.first == Long.MIN_VALUE) stringResource(R.string.start) else dateFormatter.format(Date(dateRange.first))
    val endDateText = if (dateRange.second == Long.MAX_VALUE) stringResource(R.string.end) else dateFormatter.format(Date(dateRange.second))
    val dateRangeText = if (dateRange.first == Long.MIN_VALUE && dateRange.second == Long.MAX_VALUE) {
        stringResource(R.string.all_time)
    } else {
        "$startDateText – $endDateText"
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }

    val initialPickerStartDateMillis = if (dateRange.first == Long.MIN_VALUE) null else dateRange.first
    val initialPickerEndDateMillis = if (dateRange.second == Long.MAX_VALUE) null else dateRange.second

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialPickerStartDateMillis,
        initialSelectedEndDateMillis = initialPickerEndDateMillis,
    )

    // Effect to update the picker state if the external dateRange changes
    // AND the dialog is not currently shown.
    // This handles cases where the dateRange is reset externally (e.g., by another filter).
    LaunchedEffect(dateRange, showDatePickerDialog) {
        if (!showDatePickerDialog) {
            dateRangePickerState.setSelection(
                startDateMillis = if (dateRange.first == Long.MIN_VALUE) null else dateRange.first,
                endDateMillis = if (dateRange.second == Long.MAX_VALUE) null else dateRange.second
            )
        }
    }


    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showDatePickerDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date range",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateRangeText,
                        fontSize = 14.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            TextButton(onClick = {
                onDateRangeSelected(Long.MIN_VALUE, Long.MAX_VALUE)
            }) {
                Text(stringResource(R.string.clear), style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onContactsNavigation) {
                Icon(
                    imageVector = Icons.Default.AccountBox, // Example icon
                    contentDescription = stringResource(R.string.view_contacts),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Adjust tint as needed
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent, // Your existing preference
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Customize as needed
        )
    )

    if (showDatePickerDialog) {
        val dialogColors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceDim, // Example: slightly dimmer surface
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            dayContentColor = MaterialTheme.colorScheme.onSurface, // Color for dates
            disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            disabledSelectedDayContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) // More subtle highlight
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        // If picker's selection is null, use MIN_VALUE, else use selected value
                        val selectedStart = dateRangePickerState.selectedStartDateMillis ?: Long.MIN_VALUE
                        // If picker's selection is null, use MAX_VALUE, else use selected value
                        val selectedEnd = dateRangePickerState.selectedEndDateMillis ?: Long.MAX_VALUE

                        // Ensure start is before end if both are actual dates (not MIN/MAX)
                        if (selectedStart != Long.MIN_VALUE && selectedEnd != Long.MAX_VALUE && selectedStart > selectedEnd) {
                            // Option 1: Swap them (common UX)
                            onDateRangeSelected(selectedEnd, selectedStart)
                            // Option 2: Or call with original selection and let ViewModel handle/show error
                            // onDateRangeSelected(selectedStart, selectedEnd)
                        } else {
                            onDateRangeSelected(selectedStart, selectedEnd)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },

            colors = dialogColors // Apply custom dialog colors
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = stringResource(R.string.select_date_range),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                headline = { // Displays the selected range within the picker
                    Text(
                        text = formatDatePickerHeadline(
                            dateRangePickerState.selectedStartDateMillis,
                            stringResource(R.string.start_date),
                            dateRangePickerState.selectedEndDateMillis,
                            stringResource(R.string.end_date),
                            dateFormatter
                        ),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 0.dp, bottom = 20.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                showModeToggle = true // Allow switching between calendar and input mode
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
private fun formatDatePickerHeadline(
    startMillis: Long?,
    startText: String?,
    endMillis: Long?,
    endText: String?,
    dateFormatter: SimpleDateFormat
): String {
    val startStr = startMillis?.let { dateFormatter.format(Date(it)) } ?: startText
    val endStr = endMillis?.let { dateFormatter.format(Date(it)) } ?: endText
    return "$startStr - $endStr"
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
TransactionTopBar(
    onNavigateBack = {},
    dateRange = System.currentTimeMillis() - 122323 to System.currentTimeMillis(),
    onDateRangeSelected = { _, _ -> },
    onContactsNavigation = {}
)
}


@Composable
fun ContactChip(
    contact: ContactPreview,
    onClick: () -> Unit,
    baseColor: Color = MaterialTheme.colorScheme.primary, // Generic base for amount text
    onBaseColor: Color = MaterialTheme.colorScheme.onPrimary, // Text on baseColor (not used here)
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer, // Container for avatar
    onContainerColor: Color = MaterialTheme.colorScheme.onPrimaryContainer // Text in avatar
) {
    val context = LocalContext.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer, // Overall chip container
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer, // Text on chip container
        tonalElevation = 1.dp, // Subtle elevation
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), // Adjusted padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface( // Avatar
                shape = CircleShape,
                color = containerColor, // Use tertiaryContainer for avatar background
                modifier = Modifier.size(30.dp) // Adjusted size
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy( // Smaller for avatar
                            fontWeight = FontWeight.Bold
                        ),
                        color = onContainerColor // Use onTertiaryContainer for avatar text
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    // color will be MaterialTheme.colorScheme.onSecondaryContainer (inherited)
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${AppPreferenceUtils.getCurrencyCode(context)} ${contact.amount.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = baseColor // Use tertiary for amount text to match avatar theme
                )
            }
        }
    }
}


/* ----- Data Classes and Preview ----- */


@Preview(showBackground = true, widthDp = 360)
@Composable
fun NetBalanceCardPreview() {
    val summary = UserRecordSummary("0", 2323.0, 2222.0, 101.0, 3, 2)
    val lent = listOf(
        ContactPreview("c1", "Sita Sharma", 800.0),
        ContactPreview("c2", "Ram Bahadur", 700.0),
        ContactPreview("c3", "Gita Devi", 723.0)
    )
    val borrowed = listOf(
        ContactPreview("b1", "Hari Krishna", 1200.0),
        ContactPreview("b2", "Maya Thapa", 1022.0)
    )

    AichoPaichoTheme { // Wrapped in theme
        Surface(color = MaterialTheme.colorScheme.background) { // Added Surface for better preview context
            NetBalanceCard(
                summary = summary,
                onNavigateToContactList = { /* type -> navigate */ },
                lentContacts = lent,
                borrowedContacts = borrowed,
                onContactClick = { /* id -> navigate */ }
            )
        }
    }
}
@Composable
fun TransactionFilterSection(
    selectedType: Int?,
    onTypeSelected: (Int?) -> Unit,
    fromQuery: String,
    onFromQueryChanged: (String) -> Unit,
    moneyToQuery: String,
    onMoneyToQueryChanged: (String) -> Unit,
    onMoneyFilterApplyClicked: () -> Unit,
    showCompleted: Boolean,
    onShowCompletedChanged: (Boolean) -> Unit
) {
    val fromAmount = fromQuery.toDoubleOrNull()
    val toAmount = moneyToQuery.toDoubleOrNull()
    val fromError = fromQuery.isNotBlank() && fromAmount == null
    val toError = moneyToQuery.isNotBlank() && toAmount == null
    val rangeError = fromAmount != null && toAmount != null && fromAmount > toAmount
    val hasAmountError = fromError || toError || rangeError
    var expanded by remember { mutableStateOf(false) }
    val summary = buildString {
        when (selectedType) {
            TypeConstants.LENT_ID -> append("${stringResource(R.string.lent)} • ")
            TypeConstants.BORROWED_ID -> append("${stringResource(R.string.borrowed)} • ")
        }
        if (fromQuery.isNotBlank()) append("${stringResource(R.string.from)} $fromQuery • ")
        if (moneyToQuery.isNotBlank()) append("${stringResource(R.string.to)} $moneyToQuery • ")
        if (showCompleted) append("${stringResource(R.string.show_completed)} • ")
    }.removeSuffix(" • ").trim()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.List,
                        contentDescription = stringResource(R.string.filter),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.filter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        onTypeSelected(null)
                        onFromQueryChanged("")
                        onMoneyToQueryChanged("")
                        onShowCompletedChanged(false)
                    }) {
                        Text(stringResource(R.string.clear))
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (expanded) 90f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (summary.isNotEmpty()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { onTypeSelected(null) },
                            label = { Text(stringResource(R.string.all)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == TypeConstants.LENT_ID,
                            onClick = { onTypeSelected(if (selectedType == TypeConstants.LENT_ID) null else TypeConstants.LENT_ID) },
                            label = { Text(TypeConstants.TYPE_LENT) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == TypeConstants.BORROWED_ID,
                            onClick = { onTypeSelected(if (selectedType == TypeConstants.BORROWED_ID) null else TypeConstants.BORROWED_ID) },
                            label = { Text(TypeConstants.TYPE_BORROWED) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = stringResource(R.string.amount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fromQuery,
                            onValueChange = onFromQueryChanged,
                            label = { Text(stringResource(R.string.from)) },
                            singleLine = true,
                            isError = fromError || rangeError,
                            supportingText = {
                                if (fromError) {
                                    Text(stringResource(R.string.amount_enter_number))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = moneyToQuery,
                            onValueChange = onMoneyToQueryChanged,
                            label = { Text(stringResource(R.string.to)) },
                            singleLine = true,
                            isError = toError || rangeError,
                            supportingText = {
                                if (toError) {
                                    Text(stringResource(R.string.amount_enter_number))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rangeError) {
                        Text(
                            text = stringResource(R.string.amount_invalid_range),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilterChip(
                            selected = showCompleted,
                            onClick = { onShowCompletedChanged(!showCompleted) },
                            label = { Text(stringResource(R.string.show_completed)) }
                        )
                        FilledTonalButton(
                            onClick = {
                                onMoneyFilterApplyClicked()
                                expanded = false
                            },
                            enabled = !hasAmountError
                        ) {
                            Text(stringResource(R.string.apply))
                        }
                    }
                }
            }
        }
    }
}

// ---------- Transaction Card (aligned & compact) ----------
@Composable
fun TransactionCard(
    recordWithRepayments: RecordWithRepayments,
    contact: Contact?,
    onRecordClick: () -> Unit,
    onDeleteRecord: () -> Unit,
    onNavigateToContactList: (String) -> Unit,
) {
    val record = recordWithRepayments.record
    val dateFormatter = remember { SimpleDateFormat("dd/M/yy", Locale.getDefault()) }
    val dueFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val isLent = record.typeId == TypeConstants.LENT_ID
    val accent = if (isLent) Color(0xFF2E7D32) else Color(0xFFC62828)
    val now = System.currentTimeMillis()
    val isOverdue = record.dueDate != null && record.dueDate < now && !recordWithRepayments.isSettled

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onRecordClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arrow icon (direction)
            Icon(
                painter = painterResource(
                    if (isLent) com.aspiring_creators.aichopaicho.R.drawable.arrow_up
                    else com.aspiring_creators.aichopaicho.R.drawable.arrow_down
                ),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Circle avatar (clickable) - uses contact safely
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        contact?.id?.let { onNavigateToContactList(it) }
                    },
                shape = CircleShape,
                color = accent,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contact?.name?.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact?.name ?: stringResource(R.string.unknown),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (recordWithRepayments.isSettled) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (recordWithRepayments.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormatter.format(Date(record.date)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                record.dueDate?.let { due ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOverdue) {
                            "${stringResource(R.string.overdue)} ${dueFormatter.format(Date(due))}"
                        } else {
                            "${stringResource(R.string.due)} ${dueFormatter.format(Date(due))}"
                        },
                        fontSize = 12.sp,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isLent) "+" else "-"} ${AppPreferenceUtils.getCurrencyCode(LocalContext.current)} ${recordWithRepayments.remainingAmount}",
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    textDecoration = if (recordWithRepayments.isSettled) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Completion checkbox: small and read-only
                    Checkbox(
                        checked = recordWithRepayments.isSettled,
                        onCheckedChange = null,
                        enabled = false
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TransactionFilterSectionPreview() {
    var type by remember { mutableStateOf<Int?>(2) }
    var from by remember { mutableStateOf("") }
    var money by remember { mutableStateOf("") }
    var showCompleted by remember { mutableStateOf(false) }

    TransactionFilterSection(
        selectedType = type,
        onTypeSelected = { type = it },
        fromQuery = from,
        onFromQueryChanged = { from = it },
        moneyToQuery = money,
        onMoneyToQueryChanged = { money = it },
        onMoneyFilterApplyClicked = {},
        showCompleted = showCompleted,
        onShowCompletedChanged = { showCompleted = it }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TransactionCardPreview() {
    val sampleRecord = Record(
        id = "sample",
        userId = "",
        contactId = "89474dfsdf-dfsdf",
        typeId = 1,
        amount = 2323,
        date = System.currentTimeMillis(),
        dueDate = null,
        isComplete = false,
        isDeleted = false,
        description = "",
        createdAt = 0,
        updatedAt = 0
    )
    val sampleRecordWithRepayments = RecordWithRepayments(sampleRecord, emptyList())
    TransactionCard(
        recordWithRepayments = sampleRecordWithRepayments,
        contact = Contact("c1","Golesam", userId = "",phone = listOf("987"), contactId = "", isDeleted = false, createdAt = 0, updatedAt = 0),
        onRecordClick = {},
        onDeleteRecord = {},
        onNavigateToContactList = {}
    )
}
