package com.aspiring_creators.aichopaicho.ui.component

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.aspiring_creators.aichopaicho.AppPreferenceUtils
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.*
import com.aspiring_creators.aichopaicho.data.entity.User
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme
import com.aspiring_creators.aichopaicho.viewmodel.ContactPreview
import com.aspiring_creators.aichopaicho.viewmodel.data.DashboardScreenUiState

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
                .clip(CircleShape),
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> Log.d("UserProfileImage", "Loading image: $photoUrl")
                    is AsyncImagePainter.State.Error -> Log.e("UserProfileImage", "Error loading image: $photoUrl", state.result.throwable)
                    is AsyncImagePainter.State.Success -> Log.d("UserProfileImage", "Successfully loaded image: $photoUrl")
                    else -> Log.i("UserProfileImage", "Image state: $state for url: $photoUrl")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileImagePreview() {
    AichoPaichoTheme {
        UserProfileImage(
            photoUrl = null,
            userName = "Goodness",
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextComponent(
                value = stringResource(R.string.error_detail, errorMessage),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonComponent(
                vectorLogo = Icons.Default.Refresh,
                text = stringResource(R.string.retry),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    AichoPaichoTheme {
        ErrorContent(
            errorMessage = "A network error occurred.",
            onRetry = {}
        )
    }
}

@Composable
fun UserDashboardToast(uiState: DashboardScreenUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.user?.name ?: stringResource(R.string.user_uc),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                uiState.user?.email?.let { email ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDashboardToastPreview() {
    AichoPaichoTheme {
        UserDashboardToast(
            uiState = DashboardScreenUiState(
                user = User(
                    id = "user1",
                    name = "Alex Doe",
                    email = "alex.doe@example.com",
                    photoUrl = null
                )
            )
        )
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardScreenUiState,
    onNavigateToAddTransaction: (() -> Unit)?,
    onNavigateToViewTransactions: (() -> Unit)?,
    onNavigateToSettings: (() -> Unit)?,
    onTransactionClick: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        UserDashboardToast(uiState)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            onNavigateToAddTransaction?.let { navigate ->
                QuickActionButton(
                    text = stringResource(R.string.new_txn),
                    onClick = navigate,
                    contentDescription = stringResource(R.string.add_new_transaction),
                    modifier = Modifier.weight(1f)
                )
            }
            onNavigateToViewTransactions?.let { navigate ->
                QuickActionButton(
                    text = stringResource(R.string.view_txns),
                    onClick = navigate,
                    contentDescription = stringResource(R.string.view_transactions),
                    modifier = Modifier.weight(1f)
                )
            }
            onNavigateToSettings?.let { navigate ->
                QuickActionButton(
                    text = stringResource(R.string.settings),
                    onClick = navigate,
                    contentDescription = stringResource(R.string.open_settings),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.recentLoans.isNotEmpty()) {
             Spacer(modifier = Modifier.height(24.dp))
             Text(
                text = stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
            )
            RecentTransactionsList(
                loans = uiState.recentLoans,
                onClick = onTransactionClick ?: {}
            )
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun RecentTransactionsList(
    loans: List<LoanWithContact>,
    onClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        loans.forEach { loanWithContact ->
            RecentTransactionItem(
                loan = loanWithContact.loan,
                contactName = loanWithContact.contact?.name ?: "Unknown",
                onClick = { onClick(loanWithContact.loan.id) }
            )
        }
    }
}

@Composable
fun RecentTransactionItem(
    loan: Loan,
    contactName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Themed
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = contactName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(loan.date)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${AppPreferenceUtils.getCurrencyCode(context)} ${loan.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = if (loan.typeId == TypeConstants.LENT_ID) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.net_balance),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${AppPreferenceUtils.getCurrencyCode(context)} ${summary.netTotal.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netTotal >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BalanceMiniItem(stringResource(R.string.lent), summary.totalLent, true)
                    Spacer(modifier = Modifier.width(12.dp))
                    BalanceMiniItem(stringResource(R.string.borrowed), summary.totalBorrowed, false)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 10.dp, top = 2.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BalanceItemExtended(
                            label = stringResource(R.string.lent),
                            amount = summary.totalLent,
                            isPositive = true,
                            icon = Icons.Default.KeyboardArrowUp,
                            count = summary.lentContactsCount,
                            contacts = lentContacts,
                            onNavigateToContactList = { onNavigateToContactList(TypeConstants.TYPE_LENT) },
                            onContactClick = onContactClick,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        BalanceItemExtended(
                            label = stringResource(R.string.borrowed),
                            amount = summary.totalBorrowed,
                            isPositive = false,
                            icon = Icons.Default.KeyboardArrowDown,
                            count = summary.borrowedContactsCount,
                            contacts = borrowedContacts,
                            onNavigateToContactList = { onNavigateToContactList(TypeConstants.TYPE_BORROWED) },
                            onContactClick = onContactClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceMiniItem(label: String, amount: Double, isPositive: Boolean) {
    val tint = if (isPositive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${AppPreferenceUtils.getCurrencyCode(context)} ${amount.toInt()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = tint
            )
        }
    }
}

@Composable
fun BalanceItemExtended(
    label: String,
    amount: Double,
    isPositive: Boolean,
    icon: ImageVector,
    count: Int,
    contacts: List<ContactPreview> = emptyList(),
    onNavigateToContactList: () -> Unit,
    onContactClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val amountColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val buttonContainerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val buttonContentColor = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = amountColor,
                modifier = Modifier.size(6.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${AppPreferenceUtils.getCurrencyCode(context)} ${amount.toInt()}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = amountColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onNavigateToContactList,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = buttonContainerColor,
                contentColor = buttonContentColor
            )
        ) {
            Text(
                text = if (isPositive) {
                    stringResource(
                        R.string.lent_to_n_person,
                        count,
                        if (count == 1) stringResource(R.string.person)
                        else stringResource(R.string.people)
                    )
                } else {
                    stringResource(
                        R.string.borrowed_from_n_person,
                        count,
                        if (count == 1) stringResource(R.string.person)
                        else  stringResource(R.string.people)
                    )
                },
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (contacts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(contacts.size) { idx ->
                    val c = contacts[idx]
                    ContactChip(
                        contact = c,
                        onClick = { onContactClick(c.id) },
                        baseColor = MaterialTheme.colorScheme.tertiary,
                        onBaseColor = MaterialTheme.colorScheme.onTertiary,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onContainerColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ContactChip(
    contact: ContactPreview,
    onClick: () -> Unit,
    baseColor: Color,
    onBaseColor: Color,
    containerColor: Color,
    onContainerColor: Color
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = CircleShape,
        color = containerColor,
        contentColor = onContainerColor,
        border = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Maybe add avatar if available
             Text(
                text = contact.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}
