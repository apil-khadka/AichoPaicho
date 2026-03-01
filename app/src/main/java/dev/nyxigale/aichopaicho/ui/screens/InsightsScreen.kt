package dev.nyxigale.aichopaicho.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.component.SnackbarComponent
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.formatSignedCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import dev.nyxigale.aichopaicho.viewmodel.InsightsViewModel
import dev.nyxigale.aichopaicho.viewmodel.data.MonthlyTrendPoint
import dev.nyxigale.aichopaicho.viewmodel.data.TopContactInsight
import kotlin.math.abs
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
                title = { Text(stringResource(R.string.insights), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.trend.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currency = AppPreferenceUtils.getCurrencySymbol(context)
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Overview Summary
                item {
                    InsightsOverviewCard(
                        inflow = uiState.monthlyInflow,
                        outflow = uiState.monthlyOutflow,
                        currency = currency,
                        hideAmounts = hideAmounts
                    )
                }

                // Overdue Alert
                if (uiState.overdueOutstanding > 0) {
                    item {
                        OverdueAlertCard(
                            amount = uiState.overdueOutstanding,
                            currency = currency,
                            hideAmounts = hideAmounts
                        )
                    }
                }

                // Visual Trend Chart
                item {
                    TrendChartCard(
                        trendData = uiState.trend,
                        maxTrendValue = uiState.trend.fold(0.0) { acc, item ->
                            max(acc, max(item.inflow, item.outflow))
                        }.coerceAtLeast(1.0)
                    )
                }

                // Top Contacts
                item {
                    TopContactsCard(
                        topContacts = uiState.topContacts,
                        currency = currency,
                        hideAmounts = hideAmounts
                    )
                }
            }
        }
    }
}

@Composable
fun InsightsOverviewCard(
    inflow: Double,
    outflow: Double,
    currency: String,
    hideAmounts: Boolean
) {
    val net = inflow - outflow
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(
                    label = "Inflow",
                    amount = inflow,
                    icon = Icons.Default.ArrowDownward,
                    color = Color(0xFF10B981),
                    currency = currency,
                    hideAmounts = hideAmounts
                )
                MetricItem(
                    label = "Outflow",
                    amount = outflow,
                    icon = Icons.Default.ArrowUpward,
                    color = Color(0xFFEF4444),
                    currency = currency,
                    hideAmounts = hideAmounts
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Net Balance", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatSignedCurrencyAmount(if (net >= 0) "+" else "-", currency, abs(net).toInt(), hideAmounts),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    currency: String,
    hideAmounts: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = formatCurrencyAmount(currency, amount.toInt(), hideAmounts),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OverdueAlertCard(amount: Double, currency: String, hideAmounts: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Overdue Outstanding", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    text = formatCurrencyAmount(currency, amount.toInt(), hideAmounts),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TrendChartCard(trendData: List<MonthlyTrendPoint>, maxTrendValue: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timeline, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(text = "6-Month Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (trendData.isEmpty()) {
                    Text("Not enough data for trends", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    TrendBars(trendData, maxTrendValue)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Inflow")
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = MaterialTheme.colorScheme.error, label = "Outflow")
            }
        }
    }
}

@Composable
fun TrendBars(trendData: List<MonthlyTrendPoint>, maxTrendValue: Double) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val spacing = size.width / (trendData.size)
            val barWidth = 12.dp.toPx()
            
            trendData.forEachIndexed { index, item ->
                val xBase = index * spacing + (spacing / 2)
                
                // Inflow bar
                val inflowHeight = (item.inflow / maxTrendValue * size.height).toFloat().coerceAtLeast(4f)
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(xBase - barWidth - 2.dp.toPx(), size.height - inflowHeight),
                    size = Size(barWidth, inflowHeight)
                )
                
                // Outflow bar
                val outflowHeight = (item.outflow / maxTrendValue * size.height).toFloat().coerceAtLeast(4f)
                drawRect(
                    color = errorColor,
                    topLeft = Offset(xBase + 2.dp.toPx(), size.height - outflowHeight),
                    size = Size(barWidth, outflowHeight)
                )
            }
        }
        
        // Month labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trendData.forEach { item ->
                Text(
                    text = item.label.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun TopContactsCard(topContacts: List<TopContactInsight>, currency: String, hideAmounts: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(text = "Most Active Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (topContacts.isEmpty()) {
                Text(text = "No contact data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                topContacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = contact.name.take(1).uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = contact.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = formatSignedCurrencyAmount(if (contact.netBalance >= 0) "+" else "-", currency, abs(contact.netBalance).toInt(), hideAmounts),
                            color = if (contact.netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
