package dev.nyxigale.aichopaicho.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.*
import dev.nyxigale.aichopaicho.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSyncCenter: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSecuritySetup: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as ComponentActivity
    val scope = rememberCoroutineScope()

    val availableLanguages = remember {
        mapOf("en" to "English", "ne" to "नेपाली (Nepali)")
    }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    val exportFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { settingsViewModel.exportCsvData(it) }
    }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { settingsViewModel.importCsvData(it) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        settingsViewModel.onNotificationPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        settingsViewModel.refreshNotificationPermissionStatus()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.refreshNotificationPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Section
            UserProfileCard(
                user = uiState.user,
                onSignInClick = settingsViewModel::showSignInDialog,
                onSignOutClick = settingsViewModel::showSignOutDialog
            )

            uiState.errorMessage
                ?.takeIf { it.isNotBlank() }
                ?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            // Currency Settings
            SettingsCard(
                title = "Currency",
                icon = Icons.Outlined.AddCircle
            ) {
                CurrencyDropdown (
                    selectedCurrency = uiState.selectedCurrency,
                    allCurrencies = uiState.availableCurrencies,
                    expanded = uiState.showCurrencyDropdown,
                    onToggleDropdown = settingsViewModel::toggleCurrencyDropdown,
                    onCurrencySelected = { currency ->
                        settingsViewModel.selectCurrency(currency, context)
                    }
                )
            }
            SettingsCard(
                title = "Language",
                icon = Icons.Outlined.AddCircle
            ) {
                LanguageDropDown(
                    selectedLanguageCode = uiState.selectedLanguage,
                    availableLanguages = availableLanguages,
                    expanded = languageDropdownExpanded,
                    onToggleDropdown = { languageDropdownExpanded = !languageDropdownExpanded },
                    onLanguageSelected = { newLangCode ->
                        settingsViewModel.updateLanguage(activity,  newLangCode)
                    }
                )

            }

            // Security Settings
            SettingsCard(
                title = "Security",
                icon = Icons.Default.Lock
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Protect your app with PIN or Biometric lock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onNavigateToSecuritySetup,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("App Lock Settings")
                    }
                }
            }

            // Backup & Sync Settings
            if (uiState.user?.isOffline == false) {
                SettingsCard(
                    title = stringResource(R.string.backup_sync),
                    icon = Icons.Default.ThumbUp
                ) {
                    BackupSyncSettings(
                        isBackupEnabled = uiState.isBackupEnabled,
                        onToggleBackup = settingsViewModel::toggleBackupEnabled,
                        isSyncing = uiState.isSyncing,
                        syncProgress = uiState.syncProgress,
                        syncMessage = uiState.syncMessage,
                        lastSyncTime = uiState.lastSyncTime,
                        onStartSync = settingsViewModel::startSync
                    )
                    if (uiState.syncFailedCount > 0) {
                        Text(
                            text = stringResource(R.string.sync_failed_summary, uiState.syncFailedCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = settingsViewModel::retryFailedSyncItems) {
                            Text(stringResource(R.string.retry_failed_items))
                        }
                    }
                    TextButton(onClick = onNavigateToSyncCenter) {
                        Text(stringResource(R.string.open_sync_center))
                    }
                }
            }

            SettingsCard(
                title = stringResource(R.string.reminders),
                icon = Icons.Default.ThumbUp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DueReminderSettings(
                        isEnabled = uiState.isDueReminderEnabled,
                        onToggle = settingsViewModel::toggleDueReminderEnabled
                    )

                    if (uiState.isDueReminderEnabled &&
                        uiState.requiresNotificationPermissionPrompt &&
                        !uiState.hasNotificationPermission
                    ) {
                        Text(
                            text = stringResource(R.string.notifications_permission_required_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) {
                                Text(stringResource(R.string.grant_notification_permission))
                            }

                            TextButton(
                                onClick = {
                                    openNotificationSettings(context)
                                }
                            ) {
                                Text(stringResource(R.string.open_notification_settings))
                            }
                        }
                    }
                }
            }

            SettingsCard(
                title = stringResource(R.string.analytics),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.enable_usage_analytics))
                        Switch(
                            checked = uiState.isAnalyticsEnabled,
                            onCheckedChange = { settingsViewModel.toggleAnalyticsEnabled() }
                        )
                    }
                    Text(
                        text = stringResource(R.string.analytics_toggle_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SettingsCard(
                title = stringResource(R.string.insights),
                icon = Icons.Default.Info
            ) {
                TextButton(onClick = onNavigateToInsights) {
                    Text(stringResource(R.string.open_insights))
                }
            }

            SettingsCard(
                title = stringResource(R.string.amount_privacy),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.hide_amounts))
                        Switch(
                            checked = uiState.isHideAmountsEnabled,
                            onCheckedChange = { settingsViewModel.toggleHideAmountsEnabled() }
                        )
                    }
                    Text(
                        text = stringResource(R.string.hide_amounts_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SettingsCard(
                title = stringResource(R.string.data_use_privacy),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.data_use_intro),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.data_use_contacts_only),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.data_use_local_default),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.data_use_cloud_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.data_use_manual_entry),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            uriHandler.openUri(context.getString(R.string.privacy_policy_url))
                        }) {
                            Text(stringResource(R.string.read_privacy_policy))
                        }
                        TextButton(onClick = {
                            uriHandler.openUri(context.getString(R.string.terms_of_service_url))
                        }) {
                            Text(stringResource(R.string.read_terms))
                        }
                    }
                }
            }

            SettingsCard(
                title = stringResource(R.string.data_portability),
                icon = Icons.Outlined.AddCircle
            ) {
                DataPortabilitySettings(
                    isBusy = uiState.isCsvOperationRunning,
                    statusMessage = uiState.csvOperationMessage,
                    statusLocation = uiState.csvOperationLocation,
                    onExportCsv = { exportFolderLauncher.launch(null) },
                    onImportCsv = { importFileLauncher.launch(arrayOf("text/*", "application/csv", "*/*")) }
                )
            }

            if (uiState.user?.isOffline == false) {
                SettingsCard(
                    title = stringResource(R.string.danger_zone),
                    icon = Icons.Default.Warning
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.delete_account_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = settingsViewModel::showDeleteAccountDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.delete_account))
                        }
                    }
                }
            }

            // App Information
            SettingsCard(
                title = stringResource(R.string.app_information),
                icon = Icons.Default.Info
            ) {
                AppInformation(
                    version = uiState.appVersion,
                    buildNumber = uiState.buildNumber
                )
            }

            // About Section
            SettingsCard(
                title = stringResource(R.string.about),
                icon = Icons.Default.Info
            ) {
                AboutSection()
            }
        }
    }

    if(uiState.showSignInDialog) {
        LaunchedEffect(Unit) {
            scope.launch {
                settingsViewModel.signInWithGoogle(activity)
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (uiState.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::hideSignOutDialog,
            title = { Text(stringResource(R.string.sign_out)) },
            text = {
                Text(stringResource(R.string.sign_out_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = settingsViewModel::signOut,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = settingsViewModel::hideSignOutDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (uiState.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = settingsViewModel::hideDeleteAccountDialog,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = {
                Text(stringResource(R.string.delete_account_confirm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = settingsViewModel::deleteAccount,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete_account))
                }
            },
            dismissButton = {
                TextButton(onClick = settingsViewModel::hideDeleteAccountDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Loading Overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun openNotificationSettings(context: android.content.Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    context.startActivity(intent)
}
