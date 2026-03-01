package dev.nyxigale.aichopaicho

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import dev.nyxigale.aichopaicho.ui.navigation.AppNavigationGraph
import dev.nyxigale.aichopaicho.ui.screens.LockScreen
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AichoPaichoTheme {
                var isAppUnlocked by remember { 
                    mutableStateOf(!AppPreferenceUtils.isSecurityEnabled(this)) 
                }

                if (isAppUnlocked) {
                    MainContent()
                } else {
                    LockScreen(
                        onAuthenticated = { isAppUnlocked = true }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Surface(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        AppNavigationGraph()
    }
}
