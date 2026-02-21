package dev.nyxigale.aichopaicho.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun UserProfileCard(
    user: User?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // space around the card
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // optional, helps if card height is big
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (user?.name?.isNotEmpty() == true) {
                    Text(
                        text = user.name.first().uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (user?.isOffline == true) {
                Text(
                    text = stringResource(R.string.local_account),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.sign_in_to_backup),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally), // force center
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.buttonColor)
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.logo_google),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text( stringResource(R.string.sign_in_google), color = Color.Black)
                }
            } else {
                Text(
                    text = user?.name ?: stringResource(R.string.unknown_user),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = user?.email ?: "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally), // center button
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            }
        }
    }
}


@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            content()
        }
    }
}

@Composable
fun LanguageDropDown(
    selectedLanguageCode: String,
    availableLanguages: Map<String, String>,
    expanded: Boolean,
    onToggleDropdown: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languageDisplayMap = remember { availableLanguages }

    Box {
        OutlinedButton(
            onClick = onToggleDropdown,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(languageDisplayMap[selectedLanguageCode] ?: selectedLanguageCode)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse language selection" else "Expand language selection"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onToggleDropdown,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            availableLanguages.forEach { (code, name) ->
                DropdownMenuItem(
                    onClick = {
                        onToggleDropdown()
                        onLanguageSelected(code)
                    },
                    text = { Text(name) }
                )
            }
        }
    }
}


@Composable
fun CurrencyDropdown(
    selectedCurrency: String,
    allCurrencies: List<String>, // Rename for clarity
    expanded: Boolean,
    onToggleDropdown: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter currencies based on search query
    val filteredCurrencies = if (searchQuery.isEmpty()) {
        allCurrencies
    } else {
        allCurrencies.filter {
            it.lowercase(Locale.getDefault()).contains(searchQuery.lowercase(Locale.getDefault()))
        }
    }

    Box {
        OutlinedButton(
            onClick = {
                onToggleDropdown()
                if (!expanded) { // Reset search query when opening
                    searchQuery = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.currency, selectedCurrency))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse currency selection" else "Expand currency selection"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onToggleDropdown,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer) // Set a background for the dropdown
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_currency)) },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp)) // Add some space


            if (filteredCurrencies.isEmpty()) {
                DropdownMenuItem(
                    enabled = false,
                    onClick = { },
                    text = { Text(stringResource(R.string.no_matching_currencies)) }
                )
            } else {
                filteredCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        onClick = {
                            onToggleDropdown()
                            onCurrencySelected(currency)
                                  },
                        text = { Text(currency) }
                    )
                }
            }

        }
    }
}


@Composable
fun BackupSyncSettings(
    isBackupEnabled: Boolean,
    onToggleBackup: () -> Unit,
    isSyncing: Boolean,
    syncProgress: Float,
    syncMessage: String,
    lastSyncTime: Long?,
    onStartSync: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.enable_backup))
            Switch(
                checked = isBackupEnabled,
                onCheckedChange = { onToggleBackup() }
            )
        }

        if (isBackupEnabled) {
            if (isSyncing) {
                Column {
                    LinearProgressIndicator(
                    progress = { syncProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    Text(
                        text = syncMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.manual_backup), fontSize = 14.sp)
                        lastSyncTime?.let {
                            Text(
                                text = "Last: ${dateFormatter.format(Date(it))}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onStartSync,
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.backup_now))
                    }
                }
            }
        }
    }
}

@Composable
fun DueReminderSettings(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.enable_due_reminders))
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun DataPortabilitySettings(
    isBusy: Boolean,
    statusMessage: String?,
    statusLocation: String?,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onExportCsv,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.choose_export_folder))
            }
            Button(
                onClick = onImportCsv,
                enabled = !isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.choose_import_file))
            }
        }

        if (isBusy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

        statusMessage?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        statusLocation?.let {
            Text(
                text = it,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppInformation(
    version: String,
    buildNumber: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.version))
            Text(version, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.build))
            Text(buildNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val termsOfServiceUrl = stringResource(R.string.terms_of_service_url)
    val supportWebsiteUrl = stringResource(R.string.support_website_url)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.privacy_policy),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(privacyPolicyUrl) }
                .padding(vertical = 8.dp)
        )
        Text(
            text = stringResource(R.string.terms_of_service),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(termsOfServiceUrl) }
                .padding(vertical = 8.dp)
        )
        Text(
            text = stringResource(R.string.contact_support),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(supportWebsiteUrl) }
                .padding(vertical = 8.dp)
        )
    }
}
