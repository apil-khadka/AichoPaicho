package dev.nyxigale.aichopaicho.ui.screens

import android.content.Context
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.ui.component.*
import dev.nyxigale.aichopaicho.viewmodel.AddTransactionViewModel
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiEvents
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    
                    // We need phone numbers too, query them separately
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    val phoneNumbers = mutableListOf<String>()
                    phoneCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            phoneNumbers.add(pc.getString(0))
                        }
                    }
                    
                    viewModel.onEvent(AddTransactionUiEvents.ContactSelected(
                        Contact(id = "", name = name, phone = phoneNumbers, contactId = id, userId = null)
                    ))
                }
            }
        }
    }

    LaunchedEffect(uiState.submissionSuccessful) {
        if (uiState.submissionSuccessful) {
            snackbarHostState.showSnackbar(context.getString(R.string.transaction_added_successfully))
            viewModel.clearSubmissionSuccessFlag()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(AddTransactionUiEvents.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Transaction", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }
            }
        },
        snackbarHost = { SnackbarComponent(snackbarHostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            // Type Selector
            TypeSelectorRow(
                selectedType = uiState.type,
                onTypeSelected = { viewModel.onEvent(AddTransactionUiEvents.TypeSelected(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Section
            AmountInputSection(
                amountInput = uiState.amountInput,
                onAmountChange = { viewModel.onEvent(AddTransactionUiEvents.AmountEntered(it)) },
                isError = uiState.amountError != null
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Section
            ContactSelectionSection(
                recentContacts = uiState.recentContacts,
                selectedContact = uiState.contact,
                contactNameInput = uiState.contactNameInput,
                onContactSelected = { viewModel.onEvent(AddTransactionUiEvents.ContactSelected(it)) },
                onNameChange = { viewModel.onEvent(AddTransactionUiEvents.ContactNameEntered(it)) },
                onPickContact = { contactPickerLauncher.launch(null) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Details Section
            DetailsSection(
                date = uiState.date,
                dueDate = uiState.dueDate,
                note = uiState.description ?: "",
                onDateChange = { viewModel.onEvent(AddTransactionUiEvents.DateEntered(it)) },
                onDueDateChange = { viewModel.onEvent(AddTransactionUiEvents.DueDateEntered(it)) },
                onNoteChange = { viewModel.onEvent(AddTransactionUiEvents.DescriptionEntered(it)) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TypeSelectorRow(
    selectedType: String?,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf(TypeConstants.TYPE_LENT, TypeConstants.TYPE_BORROWED)
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            types.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onTypeSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AmountInputSection(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean
) {
    val context = LocalContext.current
    val currency = AppPreferenceUtils.getCurrencyCode(context)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Amount ($currency)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        TextField(
            value = amountInput,
            onValueChange = onAmountChange,
            modifier = Modifier.widthIn(min = 120.dp, max = 240.dp),
            textStyle = MaterialTheme.typography.displayMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            ),
            placeholder = { 
                Text(
                    "0", 
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ) 
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun ContactSelectionSection(
    recentContacts: List<Contact>,
    selectedContact: Contact?,
    contactNameInput: String,
    onContactSelected: (Contact) -> Unit,
    onNameChange: (String) -> Unit,
    onPickContact: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Who is this for?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onPickContact) {
                Icon(Icons.Default.Contacts, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pick from contacts", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onPickContact() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("New", style = MaterialTheme.typography.labelSmall)
                }
            }

            items(recentContacts) { contact ->
                val isSelected = selectedContact?.id == contact.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onContactSelected(contact) }
                ) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = contact.name.split(" ").firstOrNull() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = contactNameInput,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text("Name or number") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
fun DetailsSection(
    date: Long?,
    dueDate: Long?,
    note: String,
    onDateChange: (Long) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onNoteChange: (String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DateInputField(
                    label = "Date",
                    selectedDate = date ?: System.currentTimeMillis(),
                    onDateSelected = { it?.let(onDateChange) },
                    initializeWithCurrentDate = true
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DateInputField(
                    label = "Due Date",
                    selectedDate = dueDate,
                    onDateSelected = onDueDateChange,
                    initializeWithCurrentDate = false
                )
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Note (Optional)") },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
