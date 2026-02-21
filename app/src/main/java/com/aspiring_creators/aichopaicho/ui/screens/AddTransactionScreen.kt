package com.aspiring_creators.aichopaicho.ui.screens

// import androidx.compose.foundation.layout.width // Not strictly needed for Spacer, size can be used
// import androidx.compose.material3.SnackbarHost // Replaced
// import androidx.compose.runtime.rememberCoroutineScope // Not used for snackbar here
// import com.aspiring_creators.aichopaicho.R // For R.drawable.logo_back, R.color.textColor (will be removed)
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.AmountInputField
import com.aspiring_creators.aichopaicho.ui.component.ContactPickerField
import com.aspiring_creators.aichopaicho.ui.component.DateInputField
import com.aspiring_creators.aichopaicho.ui.component.LabelComponent
import com.aspiring_creators.aichopaicho.ui.component.MultiLineTextInputField
import com.aspiring_creators.aichopaicho.ui.component.QuickActionButton
import com.aspiring_creators.aichopaicho.ui.component.SegmentedLentBorrowedToggle
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent
import com.aspiring_creators.aichopaicho.ui.component.TextComponent
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme
import com.aspiring_creators.aichopaicho.viewmodel.AddTransactionViewModel
import com.aspiring_creators.aichopaicho.viewmodel.data.AddTransactionUiEvents


@Composable
fun AddTransactionScreen(
    onNavigateBack: (() -> Unit)? = null,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by addTransactionViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMessage ->
            addTransactionViewModel.clearErrorMessage() // Clear after showing
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    LaunchedEffect(uiState.submissionSuccessful) {
        if (uiState.submissionSuccessful) {
            snackbarHostState.showSnackbar(context.getString(R.string.transaction_added_successfully))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) } // Themed snackbar
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background // Use theme background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fill the available size
                    .padding(16.dp), // Add overall padding for the content
                verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between rows
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(), // Allow title to take full width
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onNavigateBack?.let { navigateBack ->
                        IconButton( // Using IconButton for better semantics and theming
                            onClick = navigateBack,
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary // Themed icon
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp)) // Reduced spacer
                    }

                    TextComponent(
                        value = stringResource(R.string.add_new_transaction),
                        textSize = 24.sp,

                    )
                }

                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (uiState.submissionSuccessful) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(R.string.transaction_added_successfully),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Type
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelComponent(text = stringResource(R.string.type))
                    Spacer(modifier = Modifier.size(16.dp)) // Consistent spacing
                    SegmentedLentBorrowedToggle(
                        onToggle = { type ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.TypeSelected(type)
                            )
                        }
                    )
                }
                // Initialize type selection
                LaunchedEffect(Unit) {
                    if (uiState.type == null) { // Initialize only if not already set
                        addTransactionViewModel.onEvent(
                            AddTransactionUiEvents.TypeSelected(TypeConstants.TYPE_LENT)
                        )
                    }
                }


                // Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelComponent(text = stringResource(R.string.name))
                    Spacer(modifier = Modifier.size(16.dp))
                    ContactPickerField(
                        label = stringResource(R.string.contact_name), // Placeholder for OutlinedTextField
                        selectedContact = uiState.contact,
                        onContactSelected = { contact ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.ContactSelected(contact)
                            )
                        },
                        modifier = Modifier.weight(1f) // Allow field to take available space
                    )
                }

                // Amount
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelComponent(text = stringResource(R.string.amount))
                    Spacer(modifier = Modifier.size(16.dp))
                    AmountInputField(
                        label = stringResource(R.string.amount), // Placeholder
                        value = uiState.amount?.toString() ?: "",
                        onAmountTextChange = { amountStr ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.AmountEntered(amountStr)
                            )
                        },
                        isError = uiState.errorMessage != null, // Show error if present
                        errorMessage = uiState.errorMessage,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelComponent(text = stringResource(R.string.date))
                    Spacer(modifier = Modifier.size(16.dp))
                    DateInputField(
                        label = stringResource(R.string.date), // Placeholder
                        selectedDate = uiState.date,
                        onDateSelected = { date ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.DateEntered(date ?: System.currentTimeMillis()) // Provide default if null
                            )
                        },
                        initializeWithCurrentDate = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Due Date (Optional)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelComponent(text = stringResource(R.string.due_date))
                    Spacer(modifier = Modifier.size(16.dp))
                    DateInputField(
                        label = stringResource(R.string.due_date_optional),
                        selectedDate = uiState.dueDate,
                        onDateSelected = { date ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.DueDateEntered(date)
                            )
                        },
                        initializeWithCurrentDate = false,
                        modifier = Modifier.weight(1f)
                    )
                }
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

                // Description
                Row(verticalAlignment = Alignment.Top) { // Align label to top for multiline
                    LabelComponent(text = stringResource(R.string.description))
                    Spacer(modifier = Modifier.size(16.dp))
                    MultiLineTextInputField(
                        label = stringResource(R.string.description_optional), // Placeholder
                        value = uiState.description ?: "",
                        onValueChange = { description ->
                            addTransactionViewModel.onEvent(
                                AddTransactionUiEvents.DescriptionEntered(description)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // Push buttons to bottom

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Themed
                    }
                } else {
                    QuickActionButton(
                        text = stringResource(R.string.save_transaction),
                        onClick = {
                            addTransactionViewModel.onEvent(AddTransactionUiEvents.Submit)
                        },
                        contentDescription = "Save Transaction Button",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.size(8.dp)) // Spacing between buttons

                onNavigateBack?.let { navigateBack ->
                    OutlinedButton( // Using OutlinedButton for secondary action
                        onClick = {
                            if (!uiState.isLoading) navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(), // Make button full width
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary) //Themed
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionPreview() {
    AichoPaichoTheme { // Wrapped in theme
        AddTransactionScreen(onNavigateBack = {})
    }
}
