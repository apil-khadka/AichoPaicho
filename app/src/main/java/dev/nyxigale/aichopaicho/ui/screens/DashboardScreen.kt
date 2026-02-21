package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.User
import dev.nyxigale.aichopaicho.data.entity.UserRecordSummary
import dev.nyxigale.aichopaicho.ui.component.DashboardContent
import dev.nyxigale.aichopaicho.ui.component.LoadingContent
import dev.nyxigale.aichopaicho.ui.component.NetBalanceCard
import dev.nyxigale.aichopaicho.ui.component.NotSignedInContent
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.viewmodel.DashboardScreenViewModel
import dev.nyxigale.aichopaicho.viewmodel.data.DashboardScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: (() -> Unit)? = null,
    onNavigateToAddTransaction: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToViewTransactions: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToSettings: (() -> Unit)? = null, // Made nullable to match DashboardContent
    onNavigateToContactList: (String) -> Unit,
    dashboardScreenViewModel: DashboardScreenViewModel = hiltViewModel()
) {
    val uiState by dashboardScreenViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dashboardScreenViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handles general screen errors via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
            dashboardScreenViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    onNavigateToSettings?.let { navigate ->
                        IconButton(onClick = navigate) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                        uiState.user == null -> {
                            NotSignedInContent(onSignInClick = onNavigateToSettings)
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
