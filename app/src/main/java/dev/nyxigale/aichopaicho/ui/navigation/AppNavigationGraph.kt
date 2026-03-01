package dev.nyxigale.aichopaicho.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.nyxigale.aichopaicho.ui.screens.AddTransactionScreen
import dev.nyxigale.aichopaicho.ui.screens.ContactListScreen
import dev.nyxigale.aichopaicho.ui.screens.ContactTransactionScreen
import dev.nyxigale.aichopaicho.ui.screens.DashboardScreen
import dev.nyxigale.aichopaicho.ui.screens.InsightsScreen
import dev.nyxigale.aichopaicho.ui.screens.OnboardingScreen
import dev.nyxigale.aichopaicho.ui.screens.SettingsScreen
import dev.nyxigale.aichopaicho.ui.screens.SyncCenterScreen
import dev.nyxigale.aichopaicho.ui.screens.TransactionDetailScreen
import dev.nyxigale.aichopaicho.ui.screens.ViewTransactionScreen
import dev.nyxigale.aichopaicho.ui.screens.WelcomeScreen
import dev.nyxigale.aichopaicho.ui.screens.SecuritySetupScreen
import dev.nyxigale.aichopaicho.viewmodel.AppNavigationViewModel
import kotlinx.coroutines.launch

private var lastNavigationTime = 0L
private const val NAVIGATION_DEBOUNCE = 1000L

private fun canNavigate(): Boolean {
    val now = System.currentTimeMillis()
    return if (now - lastNavigationTime >= NAVIGATION_DEBOUNCE) {
        lastNavigationTime = now
        true
    } else {
        false
    }
}

private fun NavController.popSafe(): Boolean {
    if (!canNavigate()) return false
    if (currentBackStackEntry == null || previousBackStackEntry == null) return false
    return popBackStack()
}

private fun NavController.navSafe(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (!canNavigate()) return
    navigate(route, builder)
}

@Composable
fun AppNavigationGraph(
    appNavigationViewModel: AppNavigationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by appNavigationViewModel.startDestination.collectAsState()
    val scope = rememberCoroutineScope()

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            composable(Routes.ONBOARDING_SCREEN) {
                OnboardingScreen(
                    onOnboardingFinished = {
                        scope.launch {
                            appNavigationViewModel.screenViewRepository.markScreenAsShown(Routes.ONBOARDING_SCREEN)
                            navController.navSafe(Routes.WELCOME_SCREEN) {
                                popUpTo(Routes.ONBOARDING_SCREEN) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.WELCOME_SCREEN) {
                WelcomeScreen(
                    onNavigateToDashboard = {
                        navController.navSafe(Routes.DASHBOARD_SCREEN) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD_SCREEN) {
                DashboardScreen(
                    onSignOut = {
                        navController.navSafe(Routes.WELCOME_SCREEN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAddTransaction = {
                        navController.navSafe(Routes.ADD_TRANSACTION_SCREEN){
                            launchSingleTop = true
                        }
                    },
                    onNavigateToViewTransactions = {
                        navController.navSafe(Routes.VIEW_TRANSACTION_SCREEN){
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navSafe(Routes.SETTING_SCREEN){
                            launchSingleTop = true
                        }
                    },
                    onNavigateToContactList = {
                        navController.navSafe("${Routes.CONTACT_LIST_SCREEN}/$it"){
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.ADD_TRANSACTION_SCREEN) {
                AddTransactionScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    }
                )
            }

            composable(Routes.VIEW_TRANSACTION_SCREEN){
                ViewTransactionScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    },
                    onNavigateToIndividualRecord = {
                        navController.navSafe("${Routes.TRANSACTION_DETAIL_SCREEN}/$it"){
                            launchSingleTop = true
                        }
                    },
                    onNavigateToContactList ={
                        navController.navSafe("${Routes.CONTACT_TRANSACTION_SCREEN}/$it"){
                            launchSingleTop = true
                        }
                    },
                    onNavigateToContact = {
                        navController.navSafe("${Routes.CONTACT_LIST_SCREEN}/"){
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                "${Routes.TRANSACTION_DETAIL_SCREEN}/{${Routes.TRANSACTION_ID}}",
                arguments = listOf(
                    navArgument(name = Routes.TRANSACTION_ID){type = NavType.StringType}
                )
            ){
                val transactionId = it.arguments?.getString(Routes.TRANSACTION_ID)
                TransactionDetailScreen(
                    transactionId = transactionId!!,
                    onNavigateBack = {
                        navController.popSafe()
                    },
                    onNavigateToContact = { contactId ->
                        navController.navSafe("${Routes.CONTACT_TRANSACTION_SCREEN}/$contactId") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                "${Routes.CONTACT_TRANSACTION_SCREEN}/{${Routes.CONTACT_ID}}",
                arguments = listOf(
                    navArgument(name = Routes.CONTACT_ID){type = NavType.StringType}
                )
            ){
                val contactId = it.arguments?.getString(Routes.CONTACT_ID)
                ContactTransactionScreen(
                    contactId = contactId!!,
                    onNavigateBack = {
                        navController.popSafe()
                    },
                    onNavigateToRecord = {
                        navController.navSafe("${Routes.TRANSACTION_DETAIL_SCREEN}/$it"){
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                "${Routes.CONTACT_LIST_SCREEN}/{${Routes.CONTACT_LIST_TYPE}}",
                arguments = listOf(
                    navArgument(name = Routes.CONTACT_LIST_TYPE){type = NavType.StringType}
                )
            ) {
                val type = it.arguments?.getString(Routes.CONTACT_LIST_TYPE)
                ContactListScreen(
                    type = type!!,
                    onContactClicked = {
                        navController.navSafe("${Routes.CONTACT_TRANSACTION_SCREEN}/$it"){
                            launchSingleTop = false
                        }
                    },
                    onNavigateBack = {
                        navController.popSafe()
                    }
                )
            }

            composable(Routes.SETTING_SCREEN){
                SettingsScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    },
                    onNavigateToSyncCenter = {
                        navController.navSafe(Routes.SYNC_CENTER_SCREEN) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToInsights = {
                        navController.navSafe(Routes.INSIGHTS_SCREEN) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSecuritySetup = {
                        navController.navSafe(Routes.SECURITY_SETUP_SCREEN) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.SYNC_CENTER_SCREEN) {
                SyncCenterScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    }
                )
            }

            composable(Routes.INSIGHTS_SCREEN) {
                InsightsScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    }
                )
            }

            composable(Routes.SECURITY_SETUP_SCREEN) {
                SecuritySetupScreen(
                    onNavigateBack = {
                        navController.popSafe()
                    }
                )
            }

        }
    }
}
