package com.aspiring_creators.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent
import com.aspiring_creators.aichopaicho.ui.component.TransactionCard
import com.aspiring_creators.aichopaicho.ui.component.TransactionFilterSection
import com.aspiring_creators.aichopaicho.ui.component.TransactionTopBar
import com.aspiring_creators.aichopaicho.viewmodel.ViewTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTransactionScreen(
    viewTransactionViewModel: ViewTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToIndividualRecord: (String) -> Unit,
    onNavigateToContactList: (String) -> Unit,
    onNavigateToContact: () -> Unit
) {
    val uiState by viewTransactionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewTransactionViewModel.loadInitialData()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewTransactionViewModel.clearErrorMessage() // Acknowledge error
        }
    }

    Scaffold(
        topBar = {
            TransactionTopBar(
                onNavigateBack = onNavigateBack,
                dateRange = uiState.dateRange,
                onDateRangeSelected = { start, end ->
                    viewTransactionViewModel.updateDateRange(start, end)
                },
                onContactsNavigation = onNavigateToContact
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
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Themed
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TransactionFilterSection(
                            selectedType = uiState.selectedType,
                            onTypeSelected = viewTransactionViewModel::updateSelectedType,
                            fromQuery = uiState.fromQuery,
                            onFromQueryChanged = viewTransactionViewModel::updateFromQuery,
                            moneyToQuery = uiState.moneyToQuery,
                            onMoneyToQueryChanged = viewTransactionViewModel::updateMoneyToQuery,
                            onMoneyFilterApplyClicked = viewTransactionViewModel::updateMoneyFilterApplyClicked,
                            showCompleted = uiState.showCompleted,
                            onShowCompletedChanged = viewTransactionViewModel::updateShowCompleted
                        )
                    }

                    if (uiState.filteredRecords.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_transactions_match),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.filteredRecords, key = { it.record.id }) { recordWithRepayments ->
                            TransactionCard(
                                recordWithRepayments = recordWithRepayments,
                                contact = uiState.contacts[recordWithRepayments.record.contactId],
                                onRecordClick = { onNavigateToIndividualRecord(recordWithRepayments.record.id) },
                                onDeleteRecord = { viewTransactionViewModel.deleteRecord(recordWithRepayments.record.id) },
                                onNavigateToContactList = { contactId ->
                                    onNavigateToContactList(contactId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
