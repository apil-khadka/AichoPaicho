package com.aspiring_creators.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.ui.component.ContactPickerField
import com.aspiring_creators.aichopaicho.ui.component.DateInputField
import com.aspiring_creators.aichopaicho.ui.component.SegmentedLentBorrowedToggle
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.viewmodel.AddTransactionViewModel
import com.aspiring_creators.aichopaicho.viewmodel.data.AddTransactionUiEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.submissionSuccessful) {
        if (uiState.submissionSuccessful) {
            snackbarHostState.showSnackbar("Transaction added successfully")
            viewModel.clearSubmissionSuccessFlag()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearErrorMessage()
        }
    }

    // Initialize default type
    LaunchedEffect(Unit) {
        if (uiState.type == null) {
            viewModel.onEvent(AddTransactionUiEvents.TypeSelected(TypeConstants.TYPE_LENT))
        }
    }

    // Local state for inputs
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Sync local state with ViewModel state if needed (e.g. initial load or reset)
    LaunchedEffect(uiState.contact) {
        if (uiState.contact != null) {
            name = uiState.contact!!.name
            // Only update phone if not manually changed or empty?
            // For now, let's sync phone from contact if it exists
            if (uiState.contact!!.phone.isNotEmpty()) {
                phone = uiState.contact!!.phone.firstOrNull() ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Toggle
            SegmentedLentBorrowedToggle(
                onToggle = { type ->
                    viewModel.onEvent(AddTransactionUiEvents.TypeSelected(type))
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Contact Inputs
            ContactPickerField(
                label = "Contact Name",
                onContactSelected = { contact ->
                    name = contact.name
                    // Update phone if picked contact has phone
                    if (contact.phone.isNotEmpty()) {
                         phone = contact.phone.firstOrNull() ?: ""
                    }
                    viewModel.onEvent(AddTransactionUiEvents.ContactSelected(contact))
                },
                selectedContact = uiState.contact,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    // Update contact in VM with new phone
                    val currentContact = uiState.contact ?: Contact(
                         id = "",
                         name = name,
                         phone = emptyList(),
                         externalRef = null,
                         isDeleted = false,
                         createdAt = 0,
                         updatedAt = 0,
                         userId = "",
                         normalizedPhone = null
                    )
                    viewModel.onEvent(AddTransactionUiEvents.ContactSelected(
                        currentContact.copy(phone = listOf(it))
                    ))
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    viewModel.onEvent(AddTransactionUiEvents.AmountEntered(it))
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Date
            DateInputField(
                label = "Date",
                selectedDate = uiState.date,
                onDateSelected = { date ->
                    viewModel.onEvent(AddTransactionUiEvents.DateEntered(date ?: System.currentTimeMillis()))
                },
                initializeWithCurrentDate = true,
                 modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    viewModel.onEvent(AddTransactionUiEvents.DescriptionEntered(it))
                },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.onEvent(AddTransactionUiEvents.Submit) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && amountText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
