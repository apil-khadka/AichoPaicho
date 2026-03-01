package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.component.TransactionCard
import dev.nyxigale.aichopaicho.ui.component.TransactionFilterSection
import dev.nyxigale.aichopaicho.ui.component.TransactionTopBar
import dev.nyxigale.aichopaicho.viewmodel.ViewTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToIndividualRecord: (String) -> Unit,
    onNavigateToContactList: (String) -> Unit,
    onNavigateToContact: () -> Unit,
    viewTransactionViewModel: ViewTransactionViewModel = hiltViewModel()
) {
    val uiState by viewTransactionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewTransactionViewModel.loadInitialData()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewTransactionViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TransactionTopBar(
                onNavigateBack = onNavigateBack,
                dateRange = uiState.dateRange,
                onDateRangeSelected = viewTransactionViewModel::updateDateRange,
                onContactsNavigation = onNavigateToContact
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TransactionFilterSection(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewTransactionViewModel::updateSelectedType,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChanged = viewTransactionViewModel::updateSearchQuery,
                    statusFilter = uiState.statusFilter,
                    onStatusFilterChanged = viewTransactionViewModel::updateStatusFilter,
                    fromQuery = uiState.fromQuery,
                    onFromQueryChanged = viewTransactionViewModel::updateFromQuery,
                    moneyToQuery = uiState.moneyToQuery,
                    onMoneyToQueryChanged = viewTransactionViewModel::updateMoneyToQuery,
                    onMoneyFilterApplyClicked = viewTransactionViewModel::updateMoneyFilterApplyClicked
                )

                if (uiState.isLoading && uiState.records.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank())
                                stringResource(R.string.no_transactions)
                            else stringResource(R.string.no_transactions_match),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.filteredRecords, key = { it.record.id }) { recordWithRepayments ->
                            TransactionCard(
                                recordWithRepayments = recordWithRepayments,
                                contact = uiState.contacts[recordWithRepayments.record.contactId],
                                onRecordClick = { onNavigateToIndividualRecord(recordWithRepayments.record.id) },
                                onDeleteRecord = {
                                    viewTransactionViewModel.deleteRecord(recordWithRepayments.record.id)
                                },
                                onToggleComplete = { isComplete ->
                                    viewTransactionViewModel.toggleRecordCompletion(
                                        recordWithRepayments.record.id,
                                        isComplete
                                    )
                                },
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
