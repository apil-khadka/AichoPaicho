package com.aspiring_creators.aichopaicho.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme

@Composable
fun LogoTopBar(logo: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Icon(
            painter = painterResource(id = logo),
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogoTopBarPreview() {
    AichoPaichoTheme {
        LogoTopBar(logo = R.drawable.logo_aichopaicho, title = "Aicho Paicho")
    }
}

@Composable
fun TextComponent(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textSize: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = value,
        modifier = modifier.padding(8.dp),
        color = color,
        style = style,
        textAlign = textAlign,
        lineHeight = lineHeight,
        fontSize = textSize
    )
}

@Preview(showBackground = true)
@Composable
fun TextComponentPreview() {
     AichoPaichoTheme {
        TextComponent(value = "Welcome to Aicho Paicho", style = MaterialTheme.typography.headlineSmall)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonComponent(
    modifier: Modifier = Modifier,
    logo: Int? = null,
    vectorLogo: ImageVector? = null,
    text: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(56.dp), // Improved touch target
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        shape = RoundedCornerShape(16.dp), // Modern rounded shape
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (logo != null && logo != 0) {
                Icon(
                    painter = painterResource(id = logo),
                    contentDescription = text?.let { "$it logo"} ?: "Button logo",
                    modifier = Modifier.size(24.dp)
                )
            } else if (vectorLogo != null) {
                Icon(
                   imageVector = vectorLogo,
                    contentDescription = text?.let { "$it logo"} ?: "Button vector logo",
                    modifier = Modifier.size(24.dp)
                )
            }
            if(logo != null || vectorLogo != null) Spacer(modifier = Modifier.size(8.dp))

            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonComponentPreview() {
    AichoPaichoTheme {
        ButtonComponent(
            logo = R.drawable.logo_google, text = stringResource(R.string.sign_in_google),
            onClick = {}
        )
    }
}

@Composable
fun QuickActionButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    text: String
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(100.dp), // Fixed height for consistency
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 2
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExtendedQuickActionButtonPreview() {
    AichoPaichoTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            QuickActionButton(
                onClick = { /* Handle action */ },
                contentDescription = "Add new item",
                text = "Create\nTransaction",
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
    }
}

@Composable
fun SnackbarComponent(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
    ) { data ->
        Snackbar(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionOnNewLine = true,
            shape = RoundedCornerShape(12.dp),
            action = {
                data.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                }
            }
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LoadingContent(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingContextPreview() {
    AichoPaichoTheme {
        LoadingContent("Dashboard Screen...")
    }
}

@Composable
 fun NotSignedInContent(
    onSignInClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.not_signed_in),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.sign_in_to_backup),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            onSignInClick?.let { signIn ->
                ButtonComponent(
                    logo = R.drawable.logo_sign_in,
                    text = stringResource(R.string.go_to_sign_in),
                    onClick = signIn
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotSignedInContentPreview() {
    AichoPaichoTheme {
        NotSignedInContent(onSignInClick = {})
    }
}

@Composable
fun LabelComponent(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(contentPadding),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun LabelComponentView() {
    AichoPaichoTheme {
        LabelComponent("Name")
    }
}


@Composable
fun SegmentedLentBorrowedToggle(
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var isLent by remember { mutableStateOf(true) }


    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        Row {
            // Lent button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isLent) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        isLent = true
                        onToggle(TypeConstants.TYPE_LENT)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = TypeConstants.TYPE_LENT,
                    color = if (isLent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Borrowed button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (!isLent) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        isLent = false
                        onToggle(TypeConstants.TYPE_BORROWED)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = TypeConstants.TYPE_BORROWED,
                    color = if (!isLent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview
@Composable
fun SegmentedLentBorrowedTogglePreview() {
    var isLent by remember { mutableStateOf("") }
    com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme {
        SegmentedLentBorrowedToggle(
            onToggle = { isLent =  it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(22.dp))
        Text(text = stringResource(R.string.is_lent_value, isLent))
    }
}


object TypeConstants{
    const val TYPE_LENT = "Lent"
    const val LENT_ID = 1
    const val TYPE_BORROWED = "Borrowed"
    const val BORROWED_ID = 0
    fun getTypeName(value: Int): String {
        return when(value) {
            LENT_ID -> TYPE_LENT
            BORROWED_ID -> TYPE_BORROWED
            else -> "Unknown"
        }
    }
}
