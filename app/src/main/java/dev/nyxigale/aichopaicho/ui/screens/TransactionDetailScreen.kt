package dev.nyxigale.aichopaicho.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.*
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
        if (transactionId.isBlank()) {
            onNavigateBack()
            return@LaunchedEffect
        }
        transactionDetailViewModel.loadRecord(transactionId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            transactionDetailViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.isRecordDeleted) {
        if (uiState.isRecordDeleted) {
            onNavigateBack()
            transactionDetailViewModel.acknowledgeRecordDeleted()
        }
    }

    LaunchedEffect(uiState.repaymentSaved) {
        if (uiState.repaymentSaved) {
            snackbarHostState.showSnackbar(context.getString(R.string.repayment_saved_successfully))
            transactionDetailViewModel.acknowledgeRepaymentSaved()
            showAddRepaymentSheet = false
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(stringResource(R.string.delete_transaction), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_transaction_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        transactionDetailViewModel.deleteRecord()
                        showDeleteConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showAddRepaymentSheet && uiState.recordWithRepayments != null && !isEditing) {
        ModalBottomSheet(
            onDismissRequest = { showAddRepaymentSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
            Spacer(Modifier.height(24.dp))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transaction_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.recordWithRepayments != null && !uiState.isLoading) {
                        IconButton(
                            onClick = {
                                if (isEditing) transactionDetailViewModel.saveRecord()
                                isEditing = !isEditing
                                if (isEditing) showAddRepaymentSheet = false
                            },
                            modifier = Modifier.padding(end = 4.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save" else "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirmationDialog = true },
                            modifier = Modifier.padding(end = 8.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.recordWithRepayments == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                uiState.recordWithRepayments?.let { recordWithRepayments ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.widthIn(max = contentMaxWidth)) {
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

                        if (!recordWithRepayments.isSettled && !isEditing) {
                            Button(
                                onClick = { showAddRepaymentSheet = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp).widthIn(max = contentMaxWidth),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.add_repayment), fontWeight = FontWeight.Bold)
                            }
                        }

                        if (recordWithRepayments.repayments.isNotEmpty()) {
                            Box(modifier = Modifier.widthIn(max = contentMaxWidth)) {
                                RepaymentHistoryCard(repayments = recordWithRepayments.repayments)
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
