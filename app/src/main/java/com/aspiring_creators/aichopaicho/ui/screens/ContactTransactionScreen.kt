package com.aspiring_creators.aichopaicho.ui.screens

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
import androidx.compose.material3.MaterialTheme // Added
import androidx.compose.material3.Scaffold // Added
import androidx.compose.material3.SnackbarHostState // Added
import androidx.compose.material3.Surface // Added
import androidx.compose.material3.Text // Added for contact not found
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults // Added
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Added
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview // Added
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.ContactHeadingDisplay
import com.aspiring_creators.aichopaicho.ui.component.ContactRecordCard
import com.aspiring_creators.aichopaicho.ui.component.ContactRecordTabs
import com.aspiring_creators.aichopaicho.ui.component.ContactSummaryCard
import com.aspiring_creators.aichopaicho.ui.component.EmptyRecordsCard
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent // Added
import com.aspiring_creators.aichopaicho.viewmodel.ContactTransactionViewModel


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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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

                    val recordsToShow = when (uiState.selectedTab) {
                        1 -> uiState.lentRecords
                        2 -> uiState.borrowedRecords
                        else -> uiState.allRecords
                    }.filter { uiState.showCompleted || !it.isSettled } // Use new isSettled property


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
                                onDeleteRecord = { contactTransactionViewModel.deleteRecord(recordWithRepayments.record.id) },
                                onToggleComplete = { checked ->
                                    contactTransactionViewModel.toggleRecordCompletion(
                                        recordWithRepayments.record.id,
                                        checked
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
