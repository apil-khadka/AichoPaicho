package com.aspiring_creators.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.User // For Preview
import com.aspiring_creators.aichopaicho.data.entity.UserRecordSummary
import com.aspiring_creators.aichopaicho.ui.component.DashboardContent
import com.aspiring_creators.aichopaicho.ui.component.LoadingContent // From AppComponent
import com.aspiring_creators.aichopaicho.ui.component.NetBalanceCard // From DashboardComponent
import com.aspiring_creators.aichopaicho.ui.component.NotSignedInContent // From AppComponent
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme
import com.aspiring_creators.aichopaicho.viewmodel.DashboardScreenViewModel
import com.aspiring_creators.aichopaicho.viewmodel.data.DashboardScreenUiState // For Preview

@Composable
fun DashboardScreen(
    onSignOut: (() -> Unit)? = null,
    onNavigateToAddTransaction: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToViewTransactions: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToSettings: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToContactList: (String) -> Unit,
    dashboardScreenViewModel: DashboardScreenViewModel = hiltViewModel()
) {
    val uiState by dashboardScreenViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handles general screen errors via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
            dashboardScreenViewModel.clearError()
        }
    }

    // Handles automatic sign-out if user state indicates not signed in
    LaunchedEffect(uiState.isLoading, uiState.isSignedIn, uiState.user) {
        if (!uiState.isLoading && !uiState.isSignedIn && uiState.user == null) {
            onSignOut?.invoke()
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // This provides spacing
            ) {
                  item {
                    uiState.recordSummary?.let { summary ->
                        NetBalanceCard(
                            summary = summary,
                            onNavigateToContactList = onNavigateToContactList,
                        )
                    }
                }

                item {
                    when {
                        uiState.isLoading -> {
                            LoadingContent(text = stringResource(R.string.loading_dashboard))
                        }
                        !uiState.isSignedIn || uiState.user == null -> { // Simplified condition for not signed in
                            NotSignedInContent(onSignOut = onSignOut)
                        }
                        else -> {
                            DashboardContent(
                                uiState = uiState,
                                onNavigateToAddTransaction = onNavigateToAddTransaction,
                                onNavigateToViewTransactions = onNavigateToViewTransactions,
                                onNavigateToSettings = onNavigateToSettings
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview_SignedIn() {
    AichoPaichoTheme {
        val sampleUser = User("prevUser", "Preview User", "preview@example.com", null)
        val sampleSummary = UserRecordSummary("",1250.75, 2000.0, 749.25,2,3)
        val uiState = DashboardScreenUiState(
            user = sampleUser,
            recordSummary = sampleSummary,
            isLoading = false,
            isSignedIn = true,
            errorMessage = null,
        )

        DashboardScreen(
            onSignOut = {},
            onNavigateToAddTransaction = {},
            onNavigateToViewTransactions = {},
            onNavigateToSettings = {},
            onNavigateToContactList = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview_Loading() {
    AichoPaichoTheme {
        DashboardScreen(
            onSignOut = {},
            onNavigateToAddTransaction = {},
            onNavigateToViewTransactions = {},
            onNavigateToSettings = {},
            onNavigateToContactList = {}

        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview_NotSignedIn() {
    AichoPaichoTheme {

        DashboardScreen(
            onSignOut = {},
            onNavigateToAddTransaction = {},
            onNavigateToViewTransactions = {},
            onNavigateToSettings = {},
            onNavigateToContactList = {}

        )
    }
}
