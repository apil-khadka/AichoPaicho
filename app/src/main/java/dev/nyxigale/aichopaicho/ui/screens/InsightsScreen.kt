package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.formatSignedCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import dev.nyxigale.aichopaicho.viewmodel.InsightsViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    insightsViewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by insightsViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            insightsViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.insights)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.isLoading && uiState.trend.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val currency = AppPreferenceUtils.getCurrencyCode(context)
                val monthNet = uiState.monthlyInflow - uiState.monthlyOutflow
                val maxTrendValue = uiState.trend.fold(0.0) { acc, item ->
                    max(acc, max(item.inflow, item.outflow))
                }.coerceAtLeast(1.0)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.insights_overview),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                MetricRow(
                                    label = stringResource(R.string.monthly_inflow),
                                    value = formatCurrencyAmount(currency, uiState.monthlyInflow, hideAmounts)
                                )
                                MetricRow(
                                    label = stringResource(R.string.monthly_outflow),
                                    value = formatCurrencyAmount(currency, uiState.monthlyOutflow, hideAmounts)
                                )
                                MetricRow(
                                    label = stringResource(R.string.net_balance),
                                    value = formatSignedCurrencyAmount(
                                        sign = if (monthNet >= 0) "+" else "-",
                                        currency = currency,
                                        amount = kotlin.math.abs(monthNet).toInt(),
                                        hideAmounts = hideAmounts
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.overdue_outstanding),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatCurrencyAmount(currency, uiState.overdueOutstanding, hideAmounts),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.top_contacts),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (uiState.topContacts.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.no_insights_data),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    uiState.topContacts.forEach { insight ->
                                        MetricRow(
                                            label = insight.name,
                                            value = formatSignedCurrencyAmount(
                                                sign = if (insight.netBalance >= 0) "+" else "-",
                                                currency = currency,
                                                amount = kotlin.math.abs(insight.netBalance).toInt(),
                                                hideAmounts = hideAmounts
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.trend_last_6_months),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (uiState.trend.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.no_insights_data),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    uiState.trend.forEach { item ->
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            TrendRow(
                                                label = stringResource(R.string.monthly_inflow),
                                                value = item.inflow,
                                                max = maxTrendValue,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            TrendRow(
                                                label = stringResource(R.string.monthly_outflow),
                                                value = item.outflow,
                                                max = maxTrendValue,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TrendRow(
    label: String,
    value: Double,
    max: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(92.dp),
            style = MaterialTheme.typography.labelMedium
        )
        LinearProgressIndicator(
            progress = { (value / max).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = color
        )
        Text(text = value.toInt().toString(), style = MaterialTheme.typography.labelMedium)
    }
}
