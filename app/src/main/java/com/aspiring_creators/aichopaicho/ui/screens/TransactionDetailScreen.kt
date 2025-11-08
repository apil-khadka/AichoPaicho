package com.aspiring_creators.aichopaicho.ui.screens

// import androidx.compose.foundation.background // To be removed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults // Added
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // Added
import androidx.compose.material3.Scaffold // Added
import androidx.compose.material3.SnackbarHostState // Added
import androidx.compose.material3.Surface // Added
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults // Added
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
// import androidx.compose.ui.graphics.Color // To be removed
import androidx.compose.ui.tooling.preview.Preview // Added
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent // Added
import com.aspiring_creators.aichopaicho.ui.component.TransactionDetailsCard
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added
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
                        // onNavigateBack() will be called by LaunchedEffect on uiState.isRecordDeleted
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error) // Themed
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary) // Themed
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
            // Dialog colors will use MaterialTheme defaults
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
                            tint = MaterialTheme.colorScheme.onSurface // Standard M3 behavior
                        )
                    }
                },
                actions = {
                    if (uiState.record != null && !uiState.isLoading) { // Show actions only if record loaded
                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    transactionDetailViewModel.saveRecord()
                                }
                                isEditing = !isEditing // Toggle edit state
                            }
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save" else "Edit",
                                tint = if(isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant // Themed Save icon
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error // Themed Delete icon
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Themed TopAppBar
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Default for actions
                )
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background // Themed background
        ) {
            if (uiState.isLoading && uiState.record == null) { // Show loader only if record is not yet available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Themed
                }
            } else {
                uiState.record?.let { record ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TransactionDetailsCard(
                            record = record,
                            contact = uiState.contact,
                            type = uiState.type,
                            isEditing = isEditing,
                            // Assuming ViewModel's updateAmount takes String as per TransactionDetailComponent
                            onAmountChange = transactionDetailViewModel::updateAmount,
                            onDescriptionChange = transactionDetailViewModel::updateDescription,
                            onDateChange = transactionDetailViewModel::updateDate,
                            onCompletionToggle = transactionDetailViewModel::toggleCompletion
                        )
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
        // Simulate loading state. In a real app, this would be driven by ViewModel state.
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
