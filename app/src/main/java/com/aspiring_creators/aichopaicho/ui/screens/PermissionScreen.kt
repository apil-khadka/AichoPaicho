package com.aspiring_creators.aichopaicho.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.LogoTopBar
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent // Assuming SnackbarComponent is used by snackbarHostState
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added for Preview
import com.aspiring_creators.aichopaicho.viewmodel.PermissionViewModel
import kotlinx.coroutines.launch

@Composable
fun PermissionScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    permissionViewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by permissionViewModel.uiState.collectAsState()

    var contactsPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(contactsPermissionGranted) {
        permissionViewModel.setPermissionGranted(contactsPermissionGranted)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        contactsPermissionGranted = isGranted
        scope.launch {
            if (isGranted) {
                snackbarHostState.showSnackbar(context.getString(R.string.contacts_permission_granted))
                val result = permissionViewModel.grantPermissionAndProceed()
                if (result.isSuccess) {
                    onNavigateToDashboard()
                }
            } else {
                snackbarHostState.showSnackbar(context.getString(R.string.contact_permission_denied))
            }
        }
    }

    LaunchedEffect(contactsPermissionGranted) {
        if (contactsPermissionGranted && !uiState.isLoading) { // Ensure not to navigate while already processing
            val result = permissionViewModel.grantPermissionAndProceed()
            if (result.isSuccess) {
                onNavigateToDashboard()
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoTopBar(
                    logo = R.drawable.logo_contacts,
                    title = stringResource(R.string.allow_contact_access)
                )

                Spacer(modifier = Modifier.size(28.dp))

                Text(
                    text = stringResource(R.string.ask_contact_access),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = stringResource(R.string.contact_privacy_note),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.size(20.dp))

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }

                Button(
                    onClick = {
                        when {
                            contactsPermissionGranted -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.contacts_permission_already_granted))
                                    val result = permissionViewModel.grantPermissionAndProceed()
                                    if (result.isSuccess) {
                                        onNavigateToDashboard()
                                    }
                                }
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_contacts),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if (contactsPermissionGranted)
                            stringResource(R.string.permission_granted)
                        else
                            stringResource(R.string.grant_contact_access),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.grant_permission_later))
                            val result = permissionViewModel.skipPermissionAndProceed()
                            if (result.isSuccess) {
                                onNavigateToDashboard()
                            }
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.skip_for_now),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.size(20.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    AichoPaichoTheme {
        PermissionScreen(
            onNavigateToDashboard = {},
            onNavigateBack = {}
        )
    }
}
