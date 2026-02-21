package dev.nyxigale.aichopaicho.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.ui.component.AmountInputField
import dev.nyxigale.aichopaicho.ui.component.DateInputField
import dev.nyxigale.aichopaicho.ui.component.MultiLineTextInputField
import dev.nyxigale.aichopaicho.ui.component.QuickActionButton
import dev.nyxigale.aichopaicho.ui.component.SegmentedLentBorrowedToggle
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.component.StringInputField
import dev.nyxigale.aichopaicho.ui.component.TypeConstants
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.viewmodel.AddTransactionViewModel
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiEvents
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    onNavigateBack: (() -> Unit)? = null,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by addTransactionViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideLayout = configuration.screenWidthDp >= 840
    val contentMaxWidth = if (isWideLayout) 900.dp else Dp.Unspecified

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val contactUri = if (result.resultCode == Activity.RESULT_OK) result.data?.data else null
        contactUri?.let { uri ->
            val pickedContact = getContactFromUri(context, uri)
            if (pickedContact != null) {
                addTransactionViewModel.onEvent(AddTransactionUiEvents.ContactSelected(pickedContact))
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.selected_contact_has_no_phone))
                }
            }
        }
    }

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    contactPickerLauncher.launch(
                                        Intent(
                                            Intent.ACTION_PICK,
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                Text(stringResource(R.string.pick_from_contacts))
                            }
                            if (uiState.contact != null) {
                                TextButton(
                                    onClick = {
                                        addTransactionViewModel.onEvent(AddTransactionUiEvents.ContactSelected(null))
                                    },
                                    enabled = !uiState.isLoading
                                ) {
                                    Text(stringResource(R.string.clear))
                                }
                            }
                        }

                        StringInputField(
                            label = stringResource(R.string.contact_name),
                            value = uiState.contactNameInput,
                            onValueChange = { value ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.ContactNameEntered(value))
                            },
                            isError = uiState.contactNameError != null,
                            errorMessage = uiState.contactNameError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        StringInputField(
                            label = stringResource(R.string.contact_phone_number),
                            value = uiState.contactPhoneInput,
                            onValueChange = { value ->
                                addTransactionViewModel.onEvent(AddTransactionUiEvents.ContactPhoneEntered(value))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = uiState.contactPhoneError != null,
                            errorMessage = uiState.contactPhoneError,
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

private fun getContactFromUri(context: Context, contactUri: Uri): Contact? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    return try {
        val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
        cursor?.use {
            if (!it.moveToFirst()) return null

            val contactId = it.getString(
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            )
            val displayName = it.getString(
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            ) ?: ""
            val phoneNumber = it.getString(
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )?.trim()

            if (displayName.isBlank() || phoneNumber.isNullOrBlank()) return null

            Contact(
                id = UUID.randomUUID().toString(),
                name = displayName,
                userId = null,
                phone = listOf(phoneNumber),
                contactId = contactId
            )
        }
    } catch (_: SecurityException) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionPreview() {
    AichoPaichoTheme {
        AddTransactionScreen(onNavigateBack = {})
    }
}
