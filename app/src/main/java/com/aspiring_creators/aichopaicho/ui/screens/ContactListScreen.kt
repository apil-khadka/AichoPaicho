package com.aspiring_creators.aichopaicho.ui.screens

// import androidx.compose.foundation.background // To be removed
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// import androidx.compose.foundation.lazy.rememberLazyListState // For AlphabetSlider scroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card // Keep for AlphabetSlider if needed, or replace
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold // Added
import androidx.compose.material3.SnackbarHostState // Added
import androidx.compose.material3.Surface // Added
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
// import androidx.compose.material3.TextFieldDefaults // For OutlinedTextField theming if needed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Added
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.graphics.Color // To be removed
// import androidx.compose.ui.res.colorResource // To be removed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aspiring_creators.aichopaicho.R
// import androidx.compose.ui.unit.sp // Replaced by MaterialTheme.typography
// import com.aspiring_creators.aichopaicho.R // To be removed
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.ui.component.SnackbarComponent // Added
import com.aspiring_creators.aichopaicho.ui.component.TypeConstants
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added
import com.aspiring_creators.aichopaicho.viewmodel.ContactListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    type: String,
    onContactClicked: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    contactListViewModel: ContactListViewModel = hiltViewModel()
) {
    val uiState by contactListViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(type) {
        contactListViewModel.getFilteredContacts(type)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            contactListViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (type.lowercase()) {
                            TypeConstants.TYPE_LENT.lowercase() -> stringResource(R.string.contacts_you_lent_to)
                            TypeConstants.TYPE_BORROWED.lowercase() -> stringResource(R.string.contacts_you_borrowed_from)
                            else -> stringResource(R.string.all_contacts)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    onNavigateBack?.let { navigateBack ->
                        IconButton(onClick = navigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = contactListViewModel::searchContacts,
                    label = { Text(stringResource(R.string.search_contacts)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = contactListViewModel::clearSearch) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                    // Colors will adapt from MaterialTheme
                )

                Box(modifier = Modifier.weight(1f)) { // Make LazyColumn take remaining space
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (uiState.contacts.isEmpty()) { // Check the main contacts list from UiState
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (uiState.searchQuery.isNotBlank())
                                        stringResource(
                                            R.string.no_contacts_found_for,
                                            uiState.searchQuery
                                        )
                                    else stringResource(R.string.no_contacts_available_for_this_category),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                            // val listState = rememberLazyListState() // If scroll needed for AlphabetSlider
                        ) {
                            items(uiState.contacts, key = { it.id }) { contact ->
                                ContactListItem(
                                    contact = contact,
                                    onClick = { onContactClicked(contact.id) }
                                )
                            }
                        }
                    }

                    // A-Z Slider (conditionally displayed)
                    if (uiState.availableLetters.isNotEmpty() && uiState.searchQuery.isBlank() && !uiState.isLoading && uiState.contacts.isNotEmpty()) {
                        AlphabetSlider(
                            letters = uiState.availableLetters,
                            selectedLetter = uiState.selectedLetter,
                            onLetterSelected = { letter ->
                                contactListViewModel.jumpToLetter(letter)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp) // Adjusted padding
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Subtle elevation
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp), // Slightly reduced padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp) // Adjusted size
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                contact.phone.firstOrNull()?.takeIf { it.isNotBlank() }?.let { phoneNumber ->
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "View contact details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AlphabetSlider(
    letters: List<String>,
    selectedLetter: String?, // Can be null if no letter is selected
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface( // Using Surface instead of Card for a flatter, more integrated look
        modifier = modifier.width(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), // Themed background
        tonalElevation = 2.dp // Slight elevation
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp), // Padding around the column
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp) // Spacing between letters
        ) {
            items(letters) { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selectedLetter == letter) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedLetter == letter)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLetterSelected(letter) }
                        .padding(vertical = 4.dp, horizontal = 6.dp) // Padding for each letter
                )
            }
        }
    }
}




@Preview
@Composable
fun AlphabetSliderPreview() {
    AichoPaichoTheme {
        AlphabetSlider(
            letters = ('A'..'Z').map { it.toString() },
            selectedLetter = "C",
            onLetterSelected = {}
        )
    }
}

@Preview
@Composable
fun ContactListItemPreview() {
    AichoPaichoTheme {
        ContactListItem(
            contact = Contact(id = "1", name = "Zoe Zebra", phone = listOf("000-9999"), contactId = "c4", userId="u1"),
            onClick = {}
        )
    }
}
