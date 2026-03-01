package dev.nyxigale.aichopaicho.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.*
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import dev.nyxigale.aichopaicho.ui.util.formatCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.formatSignedCurrencyAmount
import dev.nyxigale.aichopaicho.ui.util.rememberHideAmountsEnabled
import dev.nyxigale.aichopaicho.viewmodel.data.ContactPreview
import dev.nyxigale.aichopaicho.viewmodel.data.DashboardScreenUiState
import dev.nyxigale.aichopaicho.viewmodel.data.UpcomingDueItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileImage(
    photoUrl: String?,
    userName: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl ?: R.drawable.placeholder_user_profile)
                .crossfade(true)
                .placeholder(R.drawable.placeholder_user_profile)
                .error(R.drawable.placeholder_user_profile_error)
                .build(),
            contentDescription = stringResource(
                R.string.profile_photo_of,
                userName ?: stringResource(R.string.user)
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
fun UserDashboardToast(uiState: DashboardScreenUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserProfileImage(
                photoUrl = uiState.user?.photoUrl?.toString(),
                userName = uiState.user?.name,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.user?.name ?: stringResource(R.string.user_uc),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                uiState.user?.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardScreenUiState,
    onNavigateToAddTransaction: (() -> Unit)?,
    onNavigateToViewTransactions: (() -> Unit)?,
    onNavigateToSettings: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        UserDashboardToast(uiState)

        if (uiState.user?.isOffline == true && uiState.isSignedIn == false) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.sign_in_to_backup_your_data_without_loss),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    onNavigateToSettings?.let { navigate ->
                        TextButton(onClick = navigate) {
                            Text(stringResource(R.string.go_to_settings_to_sign_in), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.recordSummary?.let { summary ->
            NetBalanceCard(
                summary = summary,
                onNavigateToContactList = { /* handled by dashboard or other logic if needed */ },
                onContactClick = { /* handled by dashboard or other logic if needed */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            onNavigateToAddTransaction?.let { navigate ->
                QuickActionButton(
                    text = stringResource(R.string.new_txn),
                    onClick = navigate,
                    contentDescription = stringResource(R.string.add_new_transaction),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            onNavigateToViewTransactions?.let { navigate ->
                QuickActionButton(
                    text = stringResource(R.string.view_txns),
                    onClick = navigate,
                    contentDescription = stringResource(R.string.view_transactions),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        UpcomingDueCard(items = uiState.upcomingDue)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NetBalanceCard(
    summary: UserRecordSummary,
    onNavigateToContactList: (String) -> Unit,
    lentContacts: List<ContactPreview> = emptyList(),
    borrowedContacts: List<ContactPreview> = emptyList(),
    onContactClick: (String) -> Unit = { id -> onNavigateToContactList(id) }
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()

    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.background(brush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.net_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatCurrencyAmount(
                            currency = AppPreferenceUtils.getCurrencySymbol(context),
                            amount = summary.netTotal.toInt(),
                            hideAmounts = hideAmounts
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netTotal >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        .padding(4.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BalanceItemSmall(
                            label = stringResource(R.string.lent),
                            amount = summary.totalLent,
                            isPositive = true,
                            modifier = Modifier.weight(1f)
                        )
                        BalanceItemSmall(
                            label = stringResource(R.string.borrowed),
                            amount = summary.totalBorrowed,
                            isPositive = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceItemSmall(
    label: String,
    amount: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = formatCurrencyAmount(
                currency = AppPreferenceUtils.getCurrencySymbol(context),
                amount = amount.toInt(),
                hideAmounts = hideAmounts
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun UpcomingDueCard(items: List<UpcomingDueItem>) {
    val context = LocalContext.current
    val hideAmounts = rememberHideAmountsEnabled()
    val dueFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val now = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.upcoming_due),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_upcoming_due),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.take(5).forEach { item ->
                    val isOverdue = item.dueDate < now
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.contactName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                text = if (isOverdue) {
                                    "${stringResource(R.string.overdue)} ${dueFormatter.format(Date(item.dueDate))}"
                                } else {
                                    "${stringResource(R.string.due)} ${dueFormatter.format(Date(item.dueDate))}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatSignedCurrencyAmount(
                                sign = if (item.typeId == TypeConstants.LENT_ID) "+" else "-",
                                currency = AppPreferenceUtils.getCurrencySymbol(context),
                                amount = item.amount,
                                hideAmounts = hideAmounts
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.typeId == TypeConstants.LENT_ID) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
