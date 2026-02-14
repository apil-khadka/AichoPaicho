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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme

val crimsonTextFamily = FontFamily(
    Font(R.font.crimson_regular, FontWeight.Normal),
    Font(R.font.crimson_bold, FontWeight.Bold),
    Font(R.font.crimson_italic, FontWeight.Normal, FontStyle.Italic)
)

@Composable
fun LogoTopBar(logo: Int, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Spacer(modifier = Modifier.size(36.dp))
        Icon(
            painter = painterResource(id = logo),
            contentDescription = "Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.size(36.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = crimsonTextFamily,
            modifier = Modifier.padding(start = 8.dp)

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
    modifier: Modifier = Modifier, // Added modifier parameter
    style: TextStyle = MaterialTheme.typography.bodyLarge, // Use TextStyle
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textSize: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = value,
        modifier = modifier.padding(10.dp),
        fontFamily = crimsonTextFamily,
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
        TextComponent(value = "Welcome to Aicho Paicho dfg dfg", style = MaterialTheme.typography.headlineSmall)
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
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled ,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
            if(logo != null || vectorLogo != null) Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))

            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge, // M3 style for button text
                    fontFamily = crimsonTextFamily // Keep custom font if desired
                    // color = MaterialTheme.colorScheme.onPrimary will be inherited
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
fun QuickActionButton( // This is a FAB
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    text: String
) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) // Add padding for text inside FAB
            {
                Text(
                    text = text, // Removed leading spaces, padding handles it
                    textAlign = TextAlign.Center,
                    fontFamily = crimsonTextFamily, // Keep custom font
                    style = MaterialTheme.typography.labelLarge, // M3 style for FAB text
                    maxLines = 2, // Keep maxLines
                )
            }
        }
}


@Preview(showBackground = true)
@Composable
fun ExtendedQuickActionButtonPreview() {
    AichoPaichoTheme {
        QuickActionButton(
            onClick = { /* Handle action */ },
            contentDescription = "Add new item",
            text = "Create\nItem", // Keep multi-line text
            modifier = Modifier.padding(2.dp)
        )
    }
}

@Composable
fun SnackbarComponent( // Looks good, uses M3 theme roles correctly
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
            shape = RoundedCornerShape(8.dp), // M3 uses typically smaller corner radius (e.g. 4.dp or 8.dp)
            action = {
                data.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = MaterialTheme.colorScheme.inversePrimary // Correct for action
                        )
                    }
                }
            }
        ) {
            Text(text = data.visuals.message)
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
            verticalArrangement = Arrangement.spacedBy(16.dp) // Added spacing
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            // Spacer(modifier = Modifier.height(16.dp)) // Handled by Arrangement.spacedBy
            TextComponent(
                value = text,
                style = MaterialTheme.typography.bodyLarge, // Use M3 typography
                color = MaterialTheme.colorScheme.onSurface // Explicitly use onSurface or rely on TextComponent's default
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
    onSignOut: (() -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Added spacing
        ) {
            TextComponent(
                value = stringResource(R.string.not_signed_in),
                style = MaterialTheme.typography.headlineSmall, // Use M3 typography
                color = MaterialTheme.colorScheme.onSurface // Explicitly use onSurface or rely on TextComponent's default
            )
            onSignOut?.let { signOut ->
                ButtonComponent(
                    logo = R.drawable.logo_sign_in,
                    text = stringResource(R.string.go_to_sign_in),
                    onClick = signOut
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotSignedInContentPreview() {
    AichoPaichoTheme {
        NotSignedInContent(onSignOut = {})
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
        shape = MaterialTheme.shapes.medium, // M3 shape
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(contentPadding),
            style = MaterialTheme.typography.labelLarge.copy( // Start with M3 style
                fontFamily = crimsonTextFamily, // Apply custom font
                fontWeight = FontWeight.Bold // Override fontWeight if needed for this specific label
            )
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
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Use surfaceVariant
                RoundedCornerShape(8.dp)
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
                        shape = RoundedCornerShape(6.dp)
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
                    color = if (isLent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // Use onPrimary or onSurface
                    fontWeight = if (isLent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
            }

            // Borrowed button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (!isLent) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
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
                    color = if (!isLent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // Use onPrimary or onSurface
                    fontWeight = if (!isLent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
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
            else -> "Unknown" // Added default case
        }
    }
}

