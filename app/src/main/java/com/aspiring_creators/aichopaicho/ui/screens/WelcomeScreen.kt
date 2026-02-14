package com.aspiring_creators.aichopaicho.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement // Added
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize // Added
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
// import androidx.compose.material3.Text // No longer directly used here, TextComponent is used
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.Color // No longer needed for hardcoded colors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.colorResource // No longer needed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.component.ButtonComponent
import com.aspiring_creators.aichopaicho.ui.component.LogoTopBar
import com.aspiring_creators.aichopaicho.ui.component.TextComponent
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added for Preview
import com.aspiring_creators.aichopaicho.viewmodel.WelcomeViewModel
import kotlinx.coroutines.launch
// import kotlin.math.log // Unused import

@Composable
fun WelcomeScreen(
    onNavigateToPermissions: () -> Unit,
    welcomeViewModel: WelcomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val uiState by welcomeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Auto-navigate if user is already authenticated
    LaunchedEffect(Unit) {
        if (welcomeViewModel.shouldAutoNavigate()) {
            onNavigateToPermissions()
        }
    }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.size(33.dp))

            LogoTopBar(logo = R.drawable.logo_aichopaicho, title = stringResource(R.string.app_name))

            Spacer(modifier = Modifier.size(33.dp))

            Image(
                painter = painterResource(id = R.drawable.welcome_screen_1),
                contentDescription = "welcome screen illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(400.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.size(33.dp))

            TextComponent(
                value = stringResource(R.string.never_forget_a_loan_or_a_debt),
                textSize = 28.sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(24.dp))

            // Show error message if any
            uiState.errorMessage?.let { error ->
                TextComponent(
                    value = error,
                    color = MaterialTheme.colorScheme.error,
                    textSize = 14.sp
                )
                Spacer(modifier = Modifier.size(16.dp))
            }

            // Google Sign In Button
            ButtonComponent(
                logo = R.drawable.logo_google,
                text = stringResource(R.string.sign_in_google),
                onClick = {
                    scope.launch {
                        val result = welcomeViewModel.signInWithGoogle(activity, false)
                        if (result.isSuccess) {
                            onNavigateToPermissions()
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.size(16.dp))

            // Skip Button
            ButtonComponent(
                logo = R.drawable.logo_skip,
                text = stringResource(R.string.skip_for_now),
                onClick = {
                    scope.launch {
                        val result = welcomeViewModel.skipSignIn()
                        if (result.isSuccess) {
                            onNavigateToPermissions()
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Loading indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.size(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AichoPaichoTheme { // Wrap preview in your theme
        WelcomeScreen(onNavigateToPermissions = {})
    }
}
