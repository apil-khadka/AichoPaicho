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
import androidx.compose.ui.graphics.Brush
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
import com.aspiring_creators.aichopaicho.viewmodel.data.ContactPreview
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
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        UserDashboardToast(uiState)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            onNavigateToAddTransaction?.let { navigate ->
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionButton(
                        text = stringResource(R.string.new_txn),
                        onClick = navigate,
                        contentDescription = stringResource(R.string.add_new_transaction),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            onNavigateToViewTransactions?.let { navigate ->
                 Box(modifier = Modifier.weight(1f)) {
                     QuickActionButton(
                         text = stringResource(R.string.view_txns),
                         onClick = navigate,
                         contentDescription = stringResource(R.string.view_transactions),
                         modifier = Modifier.fillMaxWidth()
                     )
                 }
            }
            onNavigateToSettings?.let { navigate ->
                 Box(modifier = Modifier.weight(1f)) {
                     QuickActionButton(
                         text = stringResource(R.string.settings),
                         onClick = navigate,
                         contentDescription = stringResource(R.string.open_settings),
                         modifier = Modifier.fillMaxWidth()
                     )
                 }
            }
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
fun NetBalanceCard(
    summary: UserRecordSummary,
    onNavigateToContactList: (String) -> Unit,
    lentContacts: List<ContactPreview> = emptyList(),
    borrowedContacts: List<ContactPreview> = emptyList(),
    onContactClick: (String) -> Unit = { id -> onNavigateToContactList(id) }
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Gradient Background
    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${AppPreferenceUtils.getCurrencyCode(context)} ${summary.netTotal.toInt()}",
                        style = MaterialTheme.typography.displaySmall, // Larger, more prominent
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netTotal >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimized view details
                    Column(horizontalAlignment = Alignment.End) {
                         Text(
                             text = stringResource(R.string.lent) + ": " + summary.totalLent.toInt(),
                             style = MaterialTheme.typography.labelMedium,
                             color = MaterialTheme.colorScheme.primary
                         )
                         Text(
                             text = stringResource(R.string.borrowed) + ": " + summary.totalBorrowed.toInt(),
                             style = MaterialTheme.typography.labelMedium,
                             color = MaterialTheme.colorScheme.error
                         )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape)
                            .padding(4.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        thickness = 1.dp,
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
                            count = summary.lentContactsCount,
                            contacts = lentContacts,
                            onNavigateToContactList = { onNavigateToContactList(TypeConstants.TYPE_LENT) },
                            onContactClick = onContactClick,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        BalanceItemExtended(
                            label = stringResource(R.string.borrowed),
                            amount = summary.totalBorrowed,
                            isPositive = false,
                            count = summary.borrowedContactsCount,
                            contacts = borrowedContacts,
                            onNavigateToContactList = { onNavigateToContactList(TypeConstants.TYPE_BORROWED) },
                            onContactClick = onContactClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceItemExtended(
    label: String,
    amount: Double,
    isPositive: Boolean,
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
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${AppPreferenceUtils.getCurrencyCode(context)} ${amount.toInt()}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onNavigateToContactList,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(48.dp)
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
