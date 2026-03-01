package dev.nyxigale.aichopaicho.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.AddRepaymentCard
import dev.nyxigale.aichopaicho.ui.component.RepaymentHistoryCard
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.component.TransactionDetailsCard
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.viewmodel.TransactionDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    transactionDetailViewModel: TransactionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToContact: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by transactionDetailViewModel.uiState.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showAddRepaymentSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape && configuration.screenWidthDp >= 700
    val contentMaxWidth = if (configuration.screenWidthDp >= 840) 1080.dp else Dp.Unspecified

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
            snackbarHostState.showSnackbar(context.getString(R.string.repayment_saved_successfully))
            transactionDetailViewModel.acknowledgeRepaymentSaved()
            showAddRepaymentSheet = false
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

    if (showAddRepaymentSheet && uiState.recordWithRepayments != null && !isEditing) {
        ModalBottomSheet(
            onDismissRequest = { showAddRepaymentSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddRepaymentCard(
                    repaymentAmount = uiState.repaymentAmount,
                    onRepaymentAmountChange = transactionDetailViewModel::onRepaymentAmountChanged,
                    repaymentDescription = uiState.repaymentDescription,
                    onRepaymentDescriptionChange = transactionDetailViewModel::onRepaymentDescriptionChanged,
                    onSaveRepayment = transactionDetailViewModel::saveRepayment,
                    isLoading = uiState.isLoading,
                    remainingAmount = uiState.recordWithRepayments?.remainingAmount ?: 0
                )
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                tonalElevation = 1.dp
            ) {
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
                                    val nextEditingState = !isEditing
                                    isEditing = nextEditingState
                                    if (nextEditingState) {
                                        showAddRepaymentSheet = false
                                    }
                                }
                            ) {
                                Icon(
                                    if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                                    contentDescription = if (isEditing) "Save" else "Edit",
                                    tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        containerColor = Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF4F5F7)
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (useTwoPane) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = contentMaxWidth)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
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
                                        onToggleComplete = transactionDetailViewModel::toggleRecordCompletion,
                                        onNavigateToContact = onNavigateToContact
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (!recordWithRepayments.isSettled && !isEditing) {
                                        Button(
                                            onClick = { showAddRepaymentSheet = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(stringResource(R.string.add_repayment))
                                        }
                                    }

                                    if (recordWithRepayments.repayments.isNotEmpty()) {
                                        RepaymentHistoryCard(repayments = recordWithRepayments.repayments)
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = contentMaxWidth)
                                    .verticalScroll(rememberScrollState())
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
                                    onToggleComplete = transactionDetailViewModel::toggleRecordCompletion,
                                    onNavigateToContact = onNavigateToContact
                                )

                                if (!recordWithRepayments.isSettled && !isEditing) {
                                    Button(
                                        onClick = { showAddRepaymentSheet = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.add_repayment))
                                    }
                                }

                                if (recordWithRepayments.repayments.isNotEmpty()) {
                                    RepaymentHistoryCard(repayments = recordWithRepayments.repayments)
                                }
                            }
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
            onNavigateBack = {},
            onNavigateToContact = {}
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
