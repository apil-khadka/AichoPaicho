package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.ContactHeadingDisplay
import dev.nyxigale.aichopaicho.ui.component.ContactRecordCard
import dev.nyxigale.aichopaicho.ui.component.ContactRecordFilterSection
import dev.nyxigale.aichopaicho.ui.component.ContactRecordTabs
import dev.nyxigale.aichopaicho.ui.component.ContactSummaryCard
import dev.nyxigale.aichopaicho.ui.component.EmptyRecordsCard
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.viewmodel.ContactTransactionViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTransactionScreen(
    contactId: String,
    contactTransactionViewModel: ContactTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRecord: (String) -> Unit
) {
    val uiState by contactTransactionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(contactId) {
        if (contactId.isBlank()){ // Ensure contactId is not blank
            onNavigateBack() // Navigate back if no valid contactId
            return@LaunchedEffect
        }
        contactTransactionViewModel.loadContactRecords(contactId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            contactTransactionViewModel.clearErrorMessage() // Acknowledge error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ContactHeadingDisplay(uiState.contact)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Simplified IconButton
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface // Standard M3 behavior
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Themed TopAppBar
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background // Themed background
        ) {
            if (uiState.isLoading && uiState.contact == null) { // Show loader only if contact details not yet loaded
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Themed
                }
            } else if (uiState.contact == null && !uiState.isLoading) { // Contact not found or error state
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: stringResource(R.string.contact_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else { // Contact loaded, show records
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ContactSummaryCard(
                            contact = uiState.contact,
                            totalLent = uiState.totalLent,
                            totalBorrowed = uiState.totalBorrowed,
                            netBalance = uiState.netBalance,
                            showCompleted = uiState.showCompleted,
                            onShowCompletedChanged = contactTransactionViewModel::updateShowCompleted
                        )
                    }

                    item {
                        ContactRecordTabs(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = contactTransactionViewModel::updateSelectedTab,
                            allCount = uiState.allRecords.size,
                            lentCount = uiState.lentRecords.size,
                            borrowedCount = uiState.borrowedRecords.size
                        )
                    }

                    item {
                        ContactRecordFilterSection(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChanged = contactTransactionViewModel::updateSearchQuery,
                            statusFilter = uiState.statusFilter,
                            onStatusFilterChanged = contactTransactionViewModel::updateStatusFilter
                        )
                    }

                    val recordsToShow = when (uiState.selectedTab) {
                        1 -> uiState.lentRecords
                        2 -> uiState.borrowedRecords
                        else -> uiState.allRecords
                    }


                    if (recordsToShow.isEmpty()) {
                        item {
                            EmptyRecordsCard()
                        }
                    } else {
                        items(recordsToShow, key = { it.record.id }) { recordWithRepayments ->
                            ContactRecordCard(
                                recordWithRepayments = recordWithRepayments,
                                type = uiState.types[recordWithRepayments.record.typeId],
                                onRecordClick = { onNavigateToRecord(recordWithRepayments.record.id) },
                                onDeleteRecord = {
                                    val deletedRecord = recordWithRepayments.record
                                    contactTransactionViewModel.deleteRecord(deletedRecord.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.transaction_deleted),
                                            actionLabel = context.getString(R.string.undo)
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            contactTransactionViewModel.restoreDeletedRecord(deletedRecord)
                                            snackbarHostState.showSnackbar(context.getString(R.string.transaction_restored))
                                        }
                                    }
                                },
                                onToggleComplete = { checked ->
                                    val originalValue = recordWithRepayments.record.isComplete
                                    contactTransactionViewModel.toggleRecordCompletion(
                                        recordWithRepayments.record.id,
                                        checked
                                    )
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(
                                                if (checked) {
                                                    R.string.transaction_marked_complete
                                                } else {
                                                    R.string.transaction_marked_incomplete
                                                }
                                            ),
                                            actionLabel = context.getString(R.string.undo)
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            contactTransactionViewModel.toggleRecordCompletion(
                                                recordWithRepayments.record.id,
                                                originalValue
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
