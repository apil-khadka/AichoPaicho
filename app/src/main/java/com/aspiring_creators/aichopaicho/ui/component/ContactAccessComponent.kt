package com.aspiring_creators.aichopaicho.ui.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // Added
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
// import androidx.compose.material3.OutlinedTextFieldDefaults // For custom colors if needed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.entity.Contact
import com.aspiring_creators.aichopaicho.ui.theme.AichoPaichoTheme // Added for previews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ContactPickerField(
    label: String,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.contact_placeholder),
    selectedContact: Contact?,
) {
    var showContactPicker by remember { mutableStateOf(false) }
    var currentValue by remember(selectedContact) { // Recomposes when selectedContact changes
        mutableStateOf(selectedContact?.name ?: "")
    }

    // If selectedContact becomes null externally (e.g. form reset), clear currentValue
    LaunchedEffect(selectedContact) {
        if (selectedContact == null) {
            currentValue = ""
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {
                          },
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { showContactPicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Select from contacts",
                            tint = MaterialTheme.colorScheme.primary // This is fine
                        )
                    }
                    if (currentValue.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                currentValue = ""
                                   val emptyContact = Contact(
                                    id = "", name = "", phone = emptyList(), contactId = null,
                                    isDeleted = false, createdAt = 0, updatedAt = 0, userId = ""
                                )
                                onContactSelected(emptyContact)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant // Good for less emphasis
                            )
                        }
                    }
                }
            }
            // Colors will be inherited from MaterialTheme
        )

        if (showContactPicker) {
            ContactPickerDialog(
                onContactSelected = { contact ->
                    currentValue = contact.name
                    onContactSelected(contact)
                    showContactPicker = false
                },
                onDismiss = {
                    showContactPicker = false
                }
            )
        }
    }
}

@Composable
fun ContactPickerDialog(
    onContactSelected: (Contact) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier // Added modifier
) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var filteredContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) } // Start with loading true
    var hasError by remember { mutableStateOf(false) }
    var permissionGrantedInitially by remember { mutableStateOf(false) }


    LaunchedEffect(searchQuery, contacts) {
        filteredContacts = if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                        contact.phone.any { it!!.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    AlertDialog(
        modifier = modifier, // Apply modifier
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_contact)) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_contacts)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, stringResource(R.string.search))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                    // Colors will be inherited
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Adjusted height
                ) {
                    ContactPermissionHandler(
                        onPermissionGranted = {
                              if(!permissionGrantedInitially) { // Load only once after initial grant
                                permissionGrantedInitially = true
                            }
                              ContactsLoader(
                                onContactsLoaded = { loadedContacts ->
                                    contacts = loadedContacts
                                    isLoading = false // Ensure loading is false after load
                                    hasError = false
                                },
                                onLoadingChange = { loading -> isLoading = loading },
                                onErrorChange = { error -> hasError = error }
                            )

                            when {
                                isLoading -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.height(8.dp))
                                            Text(stringResource(R.string.loading_contacts))
                                        }
                                    }
                                }
                                hasError -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(stringResource(R.string.failed_load_contacts), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                filteredContacts.isEmpty() && searchQuery.isNotEmpty() -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(
                                                R.string.no_contacts_matching, searchQuery
                                            )
                                        )
                                    }
                                }
                                contacts.isEmpty() && !searchQuery.isNotEmpty() -> { // Changed from filteredContacts to contacts
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                       Text(stringResource(R.string.no_contacts), textAlign = TextAlign.Center)
                                    }
                                }
                                else -> {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(filteredContacts) { contact ->
                                            ContactListItem(
                                                contact = contact,
                                                onClick = { onContactSelected(contact) },
                                                searchQuery = searchQuery
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onPermissionDenied = {
                         }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary) // Themed
            ) {
                Text(stringResource(R.string.cancel))
            }
        }

    )
}

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit,
    searchQuery: String = "",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier // Keep if necessary for specific cases
) {
    Card(
        modifier = modifier // Use the passed modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Subtle elevation
        colors = CardDefaults.cardColors( // Themed
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    buildAnnotatedString {
                        val name = contact.name
                        val startIndex = name.indexOf(searchQuery, ignoreCase = true)
                        if (startIndex >= 0) {
                            append(name.substring(0, startIndex))
                            withStyle(
                                style = SpanStyle( // Themed highlight
                                    background = MaterialTheme.colorScheme.primaryContainer,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(name.substring(startIndex, startIndex + searchQuery.length))
                            }
                            append(name.substring(startIndex + searchQuery.length))
                        } else {
                            append(name)
                        }
                    }
                } else {
                    AnnotatedString(contact.name)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
                // Color from Card's contentColor
            )

            if (contact.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                contact.phone.take(2).forEach { phoneNumber ->
                    Text(
                        text = phoneNumber!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
                    )
                }
                if (contact.phone.size > 2) {
                    Text(
                        text = stringResource(R.string.n_more, contact.phone.size - 2),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary // This is fine
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ContactPickerFieldPreview() {
    var selectedContactPreview by remember { mutableStateOf<Contact?>(null) }
    AichoPaichoTheme { // Use AichoPaichoTheme
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContactPickerField(
                label = "Contact Name",
                selectedContact = selectedContactPreview,
                onContactSelected = { contact ->
                    selectedContactPreview = if (contact.name.isEmpty()) null else contact
                    println("Selected contact: ${contact.name}")
                    if (contact.phone.isNotEmpty()) {
                        println("Phone numbers: ${contact.phone}")
                    }
                }
            )
            Text(
                "Selected: ${selectedContactPreview?.name ?: "None"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@SuppressLint("Range")
@Composable
fun ContactsLoader(
    onContactsLoaded: (List<Contact>) -> Unit,
    onLoadingChange: (Boolean) -> Unit, // Callback for loading state
    onErrorChange: (Boolean) -> Unit,   // Callback for error state
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier // Added modifier
) {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver

    LaunchedEffect(Unit) {
        onLoadingChange(true)
        onErrorChange(false)
        try {
            val contacts = withContext(Dispatchers.IO) {
                loadContactsFromDevice(contentResolver)
            }
            // Ensure onContactsLoaded is called on the main thread if it updates UI state
            withContext(Dispatchers.Main) {
                onContactsLoaded(contacts)
                onLoadingChange(false)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onErrorChange(true)
                onLoadingChange(false)
                onContactsLoaded(emptyList()) // Provide empty list on error
            }
        }
    }
    // The UI for loading/error is now handled in ContactPickerDialog based on these callbacks
}

@SuppressLint("Range")
private fun loadContactsFromDevice(contentResolver: ContentResolver): List<Contact> {
    val contactsList = mutableListOf<Contact>()
    val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )
    val cursor = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI, projection, null, null,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
    )
    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
        val nameColumn = it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val hasPhoneNumberColumn = it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
        while (it.moveToNext()) {
            val contactId = it.getString(idColumn)
            val name = it.getString(nameColumn) ?: "Unknown"
            val hasPhoneNumber = it.getInt(hasPhoneNumberColumn) > 0
            val phoneNumbers = if (hasPhoneNumber) getPhoneNumbers(contentResolver, contactId) else emptyList()
            if (phoneNumbers.isNotEmpty()) { // Only add contacts with phone numbers
                contactsList.add(
                    Contact(
                        id = contactId, // Use system contactId as a unique ID for now
                        name = name,
                        phone = phoneNumbers,
                        userId = "", // Needs to be set appropriately
                        contactId = contactId
                    )
                )
            }
        }
    }
    return contactsList
}

@SuppressLint("Range")
private fun getPhoneNumbers(contentResolver: ContentResolver, contactId: String): List<String> {
    val phoneNumbers = mutableListOf<String>()
    val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val phoneCursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneProjection,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contactId), null
    )
    phoneCursor?.use { cursor ->
        val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (cursor.moveToNext()) {
            val phoneNumber = cursor.getString(numberColumn)?.trim()
            if (!phoneNumber.isNullOrEmpty()) {
                phoneNumbers.add(phoneNumber)
            }
        }
    }
    return phoneNumbers
}

private enum class PermissionStatus {
    GRANTED,
    DENIED_SHOW_RATIONALE,
    PERMANENTLY_DENIED,
    INITIAL_CHECK,
    REQUESTING
}

@Composable
fun ContactPermissionHandler(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable () -> Unit = { PermissionDeniedUI() } // Default UI
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    // Start with INITIAL_CHECK to decide whether to show UI or request permission
    var permissionStatus by remember { mutableStateOf(PermissionStatus.INITIAL_CHECK) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) {
            PermissionStatus.GRANTED
        } else {
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) == true
            if (shouldShowRationale) PermissionStatus.DENIED_SHOW_RATIONALE else PermissionStatus.PERMANENTLY_DENIED
        }
    }

    LaunchedEffect(Unit) { // Initial check on composition
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            permissionStatus = PermissionStatus.GRANTED
        } else {
             // If not granted, immediately try to launch. UI will update based on result.
            permissionStatus = PermissionStatus.REQUESTING
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    when (permissionStatus) {
        PermissionStatus.GRANTED -> onPermissionGranted()
        PermissionStatus.DENIED_SHOW_RATIONALE -> {
            PermissionDeniedUI(canRetry = true) {
                permissionStatus = PermissionStatus.REQUESTING
                launcher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
        PermissionStatus.PERMANENTLY_DENIED -> {
            PermissionDeniedUI(canRetry = false) {
                // Action for settings button in PermissionDeniedUI will handle this
            }
        }
        PermissionStatus.INITIAL_CHECK, PermissionStatus.REQUESTING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        // null state removed as it's covered by INITIAL_CHECK
    }
}

@Composable
private fun PermissionDeniedUI(
    canRetry: Boolean = true,
    onRetryClick: () -> Unit = {} // Used by "Try Again"
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Phone, null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.contact_permission_required),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (canRetry) stringResource(R.string.request_permission_explanation)
            else stringResource(R.string.permission_denied_explanation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton( // M3 component, colors will adapt
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Filled.Settings, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings))
            }
            if (canRetry) {
                Button(onClick = onRetryClick) { // M3 component, colors will adapt
                    Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.try_again))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.note_manual_contact),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary // Fine for emphasis
        )
    }
}


@Composable
fun ContactItem( // General purpose contact item
    contact: Contact,
    onClick: ((Contact) -> Unit)? = null,
    modifier: Modifier = Modifier // Added modifier
) {
    Card(
        modifier = modifier // Use passed modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick(contact) } else it },
        colors = CardDefaults.cardColors( // Themed
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person, null,
                    tint = MaterialTheme.colorScheme.primary // Fine
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    contact.name,
                    style = MaterialTheme.typography.titleMedium, // Adjusted style
                    fontWeight = FontWeight.Medium
                    // Color from Card's contentColor
                )
            }
            if (contact.phone.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                contact.phone.forEach { phoneNumber ->
                    phoneNumber.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 36.dp) // Indent phone numbers
                        ) {
                            Icon(
                                Icons.Default.Phone, null, Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant // Themed
                            )
                            Spacer(Modifier.width(8.dp))
                            if (it != null) {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium, // Adjusted style
                                    color = MaterialTheme.colorScheme.onSurfaceVariant // Themed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactItemPreview() {
    AichoPaichoTheme { // Use AichoPaichoTheme
        ContactItem(
            contact = Contact(
                id = "1", name = "John Doe", userId = "",
                phone = listOf("+1234567890", "+0987654321"), contactId = "123"
            )
        )
    }
}


fun openContactDetailsByPhoneNumber(context: Context, phoneNumber: String?) {
    if (phoneNumber.isNullOrBlank()) {
        println("Phone number is invalid.")
        // Optionally: Toast.makeText(context, "No phone number available for this contact", Toast.LENGTH_SHORT).show()
        return
    }

    var contactId: Long? = null
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
    val projection = arrayOf(ContactsContract.PhoneLookup._ID)

    try {
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndex(ContactsContract.PhoneLookup._ID)
                if (idColumn != -1) {
                    contactId = it.getLong(idColumn)
                }
            }
        }
    } catch (e: Exception) {
        println("Error querying for contact by phone number: ${e.message}")
        // Optionally: Toast.makeText(context, "Error finding contact", Toast.LENGTH_SHORT).show()
        return
    }


    if (contactId != null) {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId!!)
        val intent = Intent(Intent.ACTION_VIEW, contactUri)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            println("No app found to open contact details.")
        }
    } else {
        println("Contact with phone number $phoneNumber not found on this device.")
        // Optionally: Toast.makeText(context, "Contact not found on this device", Toast.LENGTH_SHORT).show()
    }
}
