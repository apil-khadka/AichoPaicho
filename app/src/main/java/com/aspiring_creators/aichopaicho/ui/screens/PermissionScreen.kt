package com.aspiring_creators.aichopaicho.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
// import androidx.compose.ui.res.colorResource // No longer needed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.ButtonComponent
import com.aspiring_creators.aichopaicho.ui.component.LogoTopBar
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent // Assuming SnackbarComponent is used by snackbarHostState
import com.aspiring_creators.aichopaicho.ui.component.TextComponent
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
        snackbarHost = {
            // Use your themed SnackbarComponent if you have one, or stick to Material's SnackbarHost
            // For this example, assuming SnackbarComponent is set up to be themed
            SnackbarComponent(snackbarHostState = snackbarHostState)
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Added overall padding
            color = MaterialTheme.colorScheme.background // Use theme background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LogoTopBar(
                    logo = R.drawable.logo_contacts, // Consider a theme-able icon if needed
                    title = stringResource(R.string.allow_contact_access)
                )

                Spacer(modifier = Modifier.size(60.dp)) // Adjusted Spacing

                TextComponent(
                    value = stringResource(R.string.ask_contact_access),
                    textSize = 24.sp, // Adjusted size
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.size(60.dp)) // Adjusted Spacing

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp), // Add padding below error
                        textAlign = TextAlign.Center
                    )
                }

                ButtonComponent(
                    logo = R.drawable.logo_contacts, // Icon for contacts
                    text = if (contactsPermissionGranted) stringResource(R.string.permission_granted) else stringResource(
                        R.string.grant_contact_access
                    ),
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
                    modifier = Modifier.fillMaxWidth(0.8f) // Consistent button width
                )

                Spacer(modifier = Modifier.size(16.dp))

                ButtonComponent(
                    logo = R.drawable.logo_skip, // Icon for skip
                    text = stringResource(R.string.skip_for_now),
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
                    modifier = Modifier.fillMaxWidth(0.8f) // Consistent button width
                )


                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Use theme primary
                    }
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
