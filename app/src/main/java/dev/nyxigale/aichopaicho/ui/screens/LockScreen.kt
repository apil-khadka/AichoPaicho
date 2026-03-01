package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.nyxigale.aichopaicho.viewmodel.SecurityViewModel

@Composable
fun LockScreen(
    onAuthenticated: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticated()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.isBiometricEnabled && activity != null) {
            viewModel.authenticateWithBiometric(activity) { success ->
                if (success) onAuthenticated()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        // PIN Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isFilled = index < uiState.enteredPin.length
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

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Numeric Keypad
        NumericKeypad(
            onDigitClick = { viewModel.onPinInput(it) },
            onDeleteClick = { viewModel.onPinDelete() },
            showBiometric = uiState.isBiometricEnabled,
            onBiometricClick = {
                if (activity != null) {
                    viewModel.authenticateWithBiometric(activity) { success ->
                        if (success) onAuthenticated()
                    }
                }
            }
        )
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    showBiometric: Boolean = false,
    onBiometricClick: () -> Unit = {}
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (showBiometric) "BIO" else "", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { digit ->
                    when (digit) {
                        "DEL" -> KeypadButton(
                            onClick = onDeleteClick,
                            content = {
                                Icon(Icons.AutoMirrored.Filled.Backspace, null)
                            }
                        )
                        "BIO" -> KeypadButton(
                            onClick = onBiometricClick,
                            content = {
                                Icon(Icons.Default.Fingerprint, null)
                            }
                        )
                        "" -> Spacer(modifier = Modifier.size(72.dp))
                        else -> KeypadButton(
                            onClick = { onDigitClick(digit) },
                            text = digit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    onClick: () -> Unit,
    text: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )
            } else if (content != null) {
                content()
            }
        }
    }
}
