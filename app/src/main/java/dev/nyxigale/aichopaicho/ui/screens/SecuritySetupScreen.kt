package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.nyxigale.aichopaicho.viewmodel.SecuritySetupStep
import dev.nyxigale.aichopaicho.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Setup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.setupStep) {
                SecuritySetupStep.NONE -> {
                    SecurityOverviewPage(
                        isSecurityEnabled = uiState.isSecurityEnabled,
                        isBiometricEnabled = uiState.isBiometricEnabled,
                        canUseBiometric = viewModel.canUseBiometric(),
                        onEnablePin = { viewModel.startSetup() },
                        onToggleBiometric = { viewModel.toggleBiometric(it) },
                        onDisableSecurity = { viewModel.disableSecurity() }
                    )
                }
                SecuritySetupStep.ENTER_NEW_PIN -> {
                    PinEntryPage(
                        title = "Create a PIN",
                        subtitle = "Enter a 4-digit PIN to secure your app",
                        enteredPin = uiState.enteredPin,
                        error = uiState.error,
                        onDigitClick = { viewModel.onSetupPinInput(it) },
                        onDeleteClick = { viewModel.onPinDelete() }
                    )
                }
                SecuritySetupStep.CONFIRM_NEW_PIN -> {
                    PinEntryPage(
                        title = "Confirm PIN",
                        subtitle = "Enter the PIN again to confirm",
                        enteredPin = uiState.enteredPin,
                        error = uiState.error,
                        onDigitClick = { viewModel.onSetupPinInput(it) },
                        onDeleteClick = { viewModel.onPinDelete() }
                    )
                }
                SecuritySetupStep.SUCCESS -> {
                    SuccessPage(onDone = onNavigateBack)
                }
            }
        }
    }
}

@Composable
fun SecurityOverviewPage(
    isSecurityEnabled: Boolean,
    isBiometricEnabled: Boolean,
    canUseBiometric: Boolean,
    onEnablePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onDisableSecurity: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (isSecurityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isSecurityEnabled) "App Security is ON" else "Secure Your App",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Keep your financial transactions private with PIN or Biometric authentication.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (!isSecurityEnabled) {
            Button(
                onClick = onEnablePin,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Setup PIN Lock")
            }
        } else {
            // Biometric Toggle
            if (canUseBiometric) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Fingerprint, null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Biometric", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Unlock using fingerprint or face",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onToggleBiometric
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = onDisableSecurity,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Disable Security")
            }
        }
    }
}

@Composable
fun PinEntryPage(
    title: String,
    subtitle: String,
    enteredPin: String,
    error: String?,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val isFilled = index < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(48.dp))
        NumericKeypad(onDigitClick = onDigitClick, onDeleteClick = onDeleteClick)
    }
}

@Composable
fun SuccessPage(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Security Setup Complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done")
        }
    }
}
