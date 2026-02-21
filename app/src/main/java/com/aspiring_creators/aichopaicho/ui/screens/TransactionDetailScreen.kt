package com.aspiring_creators.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.AddRepaymentCard
import com.aspiring_creators.aichopaicho.ui.component.RepaymentHistoryCard
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent
import com.aspiring_creators.aichopaicho.ui.component.TransactionDetailsCard
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme
import com.aspiring_creators.aichopaicho.viewmodel.TransactionDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    transactionDetailViewModel: TransactionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by transactionDetailViewModel.uiState.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(transactionId) {
        if (transactionId.isBlank()) { // Check for blank instead of just empty
            onNavigateBack()
            return@LaunchedEffect
        }
        transactionDetailViewModel.loadRecord(transactionId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            transactionDetailViewModel.clearErrorMessage() // Acknowledge
        }
    }
     LaunchedEffect(uiState.isRecordDeleted) {
        if (uiState.isRecordDeleted) { // Check for explicit true
            snackbarHostState.showSnackbar(context.getString(R.string.transaction_deleted))
            transactionDetailViewModel.acknowledgeRecordDeleted() // Reset flag
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.repaymentSaved) {
        if (uiState.repaymentSaved) {
            snackbarHostState.showSnackbar("Repayment saved successfully!")
            transactionDetailViewModel.acknowledgeRepaymentSaved()
            // The ViewModel's flow should automatically emit the new state,
            // but a manual reload can be a fallback if needed.
            // transactionDetailViewModel.loadRecord(transactionId)
        }
    }


    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(stringResource(R.string.delete_transaction)) },
            text = { Text(stringResource(R.string.delete_transaction_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionDetailViewModel.deleteRecord()
                        showDeleteConfirmationDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transaction_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState.recordWithRepayments != null && !uiState.isLoading) {
                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    transactionDetailViewModel.saveRecord()
                                }
                                isEditing = !isEditing
                            }
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save" else "Edit",
                                tint = if(isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading && uiState.recordWithRepayments == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                uiState.recordWithRepayments?.let { recordWithRepayments ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Make the column scrollable
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TransactionDetailsCard(
                            recordWithRepayments = recordWithRepayments,
                            contact = uiState.contact,
                            type = uiState.type,
                            isEditing = isEditing,
                            onAmountChange = transactionDetailViewModel::updateAmount,
                            onDescriptionChange = transactionDetailViewModel::updateDescription,
                            onDateChange = transactionDetailViewModel::updateDate,
                            onDueDateChange = transactionDetailViewModel::updateDueDate,
                            onToggleComplete = transactionDetailViewModel::toggleRecordCompletion
                        )

                        // Only show AddRepaymentCard if not fully settled and not in edit mode
                        if (!recordWithRepayments.isSettled && !isEditing) {
                            AddRepaymentCard(
                                repaymentAmount = uiState.repaymentAmount,
                                onRepaymentAmountChange = transactionDetailViewModel::onRepaymentAmountChanged,
                                repaymentDescription = uiState.repaymentDescription,
                                onRepaymentDescriptionChange = transactionDetailViewModel::onRepaymentDescriptionChanged,
                                onSaveRepayment = transactionDetailViewModel::saveRepayment,
                                isLoading = uiState.isLoading,
                                remainingAmount = recordWithRepayments.remainingAmount
                            )
                        }

                        // Display repayment history if any
                        if (recordWithRepayments.repayments.isNotEmpty()) {
                            RepaymentHistoryCard(repayments = recordWithRepayments.repayments)
                        }
                    }
                } ?: Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: stringResource(R.string.transaction_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailScreenPreview_ViewMode() {
    AichoPaichoTheme {
        TransactionDetailScreen(
            transactionId = "previewIdView",
            onNavigateBack = {}

        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TransactionDetailScreenPreview_Loading() {
    AichoPaichoTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transaction Details") },
                    navigationIcon = {
                        IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
