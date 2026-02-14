package com.aspiring_creators.aichopaicho.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.recreate
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.AboutSection
import com.aspiring_creators.aichopaicho.ui.component.AppInformation
import com.aspiring_creators.aichopaicho.ui.component.BackupSyncSettings
import com.aspiring_creators.aichopaicho.ui.component.CurrencyDropdown
import com.aspiring_creators.aichopaicho.ui.component.LanguageDropDown
import com.aspiring_creators.aichopaicho.ui.component.SettingsCard
import com.aspiring_creators.aichopaicho.ui.component.UserProfileCard
import com.aspiring_creators.aichopaicho.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val scope = rememberCoroutineScope()

    val availableLanguages = remember {
        mapOf("en" to "English", "ne" to "नेपाली (Nepali)")
    }
    var languageDropdownExpanded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.appThemeColor))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
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

