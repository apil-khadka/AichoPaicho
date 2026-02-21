package dev.nyxigale.aichopaicho.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.AmountInputField
import dev.nyxigale.aichopaicho.ui.component.ContactPickerField
import dev.nyxigale.aichopaicho.ui.component.DateInputField
import dev.nyxigale.aichopaicho.ui.component.MultiLineTextInputField
import dev.nyxigale.aichopaicho.ui.component.QuickActionButton
import dev.nyxigale.aichopaicho.ui.component.SegmentedLentBorrowedToggle
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.viewmodel.AddTransactionViewModel
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiEvents
import dev.nyxigale.aichopaicho.viewmodel.data.RecurrenceType

@Composable
fun AddTransactionScreen(
    onNavigateBack: (() -> Unit)? = null,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by addTransactionViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideLayout = configuration.screenWidthDp >= 840
    val contentMaxWidth = if (isWideLayout) 900.dp else Dp.Unspecified

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMessage ->
            addTransactionViewModel.clearErrorMessage()
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    LaunchedEffect(uiState.submissionSuccessful) {
        if (uiState.submissionSuccessful) {
            snackbarHostState.showSnackbar(context.getString(R.string.transaction_added_successfully))
            addTransactionViewModel.clearSubmissionSuccessFlag()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.type == null) {
            addTransactionViewModel.onEvent(AddTransactionUiEvents.TypeSelected(TypeConstants.TYPE_LENT))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = contentMaxWidth)
                        .padding(horizontal = if (isWideLayout) 24.dp else 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        onNavigateBack?.let { navigateBack ->
                            IconButton(onClick = navigateBack, enabled = !uiState.isLoading) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(
                            text = stringResource(R.string.add_new_transaction),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = stringResource(R.string.type), style = MaterialTheme.typography.labelLarge)
                        SegmentedLentBorrowedToggle(
                            onToggle = { type ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.TypeSelected(type))
                            }
                        )

                        HorizontalDivider()

                        Text(text = stringResource(R.string.contact), style = MaterialTheme.typography.labelLarge)
                        ContactPickerField(
                            label = stringResource(R.string.contact_name),
                            selectedContact = uiState.contact,
                            onContactSelected = { contact ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.ContactSelected(contact))
                            },
                            isError = uiState.contactError != null,
                            errorMessage = uiState.contactError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = stringResource(R.string.amount), style = MaterialTheme.typography.labelLarge)
                        AmountInputField(
                            label = stringResource(R.string.amount),
                            value = uiState.amountInput,
                            onAmountTextChange = { amountStr ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.AmountEntered(amountStr))
                            },
                            isError = uiState.amountError != null,
                            errorMessage = uiState.amountError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = stringResource(R.string.date), style = MaterialTheme.typography.labelLarge)
                        DateInputField(
                            label = stringResource(R.string.date),
                            selectedDate = uiState.date,
                            onDateSelected = { date ->
                                addTransactionViewModel.onEvent(
                                    AddTransactionUiEvents.DateEntered(date ?: System.currentTimeMillis())
                                )
                            },
                            initializeWithCurrentDate = true,
                            isError = uiState.dateError != null,
                            errorMessage = uiState.dateError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = stringResource(R.string.due_date), style = MaterialTheme.typography.labelLarge)
                        DateInputField(
                            label = stringResource(R.string.due_date_optional),
                            selectedDate = uiState.dueDate,
                            onDateSelected = { date ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.DueDateEntered(date))
                            },
                            initializeWithCurrentDate = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.dueDate != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        addTransactionViewModel.onEvent(AddTransactionUiEvents.DueDateEntered(null))
                                    }
                                ) {
                                    Text(stringResource(R.string.clear_due_date))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.make_recurring),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Switch(
                                        checked = uiState.isRecurringEnabled,
                                        onCheckedChange = { isEnabled ->
                                            addTransactionViewModel.onEvent(
                                                AddTransactionUiEvents.RecurringEnabledChanged(isEnabled)
                                            )
                                        }
                                    )
                                }

                                if (uiState.isRecurringEnabled) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(RecurrenceType.DAILY, RecurrenceType.WEEKLY).forEach { recurrenceType ->
                                            FilterChip(
                                                selected = uiState.recurrenceType == recurrenceType,
                                                onClick = {
                                                    addTransactionViewModel.onEvent(
                                                        AddTransactionUiEvents.RecurrenceSelected(recurrenceType)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        when (recurrenceType) {
                                                            RecurrenceType.DAILY -> stringResource(R.string.daily)
                                                            RecurrenceType.WEEKLY -> stringResource(R.string.weekly)
                                                            else -> recurrenceType.name
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(RecurrenceType.MONTHLY, RecurrenceType.CUSTOM).forEach { recurrenceType ->
                                            FilterChip(
                                                selected = uiState.recurrenceType == recurrenceType,
                                                onClick = {
                                                    addTransactionViewModel.onEvent(
                                                        AddTransactionUiEvents.RecurrenceSelected(recurrenceType)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        when (recurrenceType) {
                                                            RecurrenceType.MONTHLY -> stringResource(R.string.monthly)
                                                            RecurrenceType.CUSTOM -> stringResource(R.string.custom)
                                                            else -> recurrenceType.name
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    if (uiState.recurrenceType == RecurrenceType.CUSTOM) {
                                        AmountInputField(
                                            label = stringResource(R.string.every_n_days),
                                            value = uiState.customRecurrenceDays,
                                            onAmountTextChange = { input ->
                                                addTransactionViewModel.onEvent(
                                                    AddTransactionUiEvents.CustomRecurrenceDaysEntered(input)
                                                )
                                            },
                                            isError = uiState.customRecurrenceDaysError != null,
                                            errorMessage = uiState.customRecurrenceDaysError,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Text(text = stringResource(R.string.description), style = MaterialTheme.typography.labelLarge)
                        MultiLineTextInputField(
                            label = stringResource(R.string.description_optional),
                            value = uiState.description ?: "",
                            onValueChange = { description ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.DescriptionEntered(description))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        if (isLandscape && onNavigateBack != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionButton(
                                    text = stringResource(R.string.save_transaction),
                                    onClick = {
                                        addTransactionViewModel.onEvent(AddTransactionUiEvents.Submit)
                                    },
                                    contentDescription = "Save Transaction Button",
                                    buttonHeight = 64.dp,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { onNavigateBack() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        } else {
                            QuickActionButton(
                                text = stringResource(R.string.save_transaction),
                                onClick = {
                                    addTransactionViewModel.onEvent(AddTransactionUiEvents.Submit)
                                },
                                contentDescription = "Save Transaction Button",
                                buttonHeight = 64.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            onNavigateBack?.let { navigateBack ->
                                OutlinedButton(
                                    onClick = {
                                        if (!uiState.isLoading) navigateBack()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionPreview() {
    AichoPaichoTheme {
        AddTransactionScreen(onNavigateBack = {})
    }
}
