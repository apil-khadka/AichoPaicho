package dev.nyxigale.aichopaicho.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.ui.theme.AichoPaichoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StringInputField(
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    value: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
        )
    )
}

@Preview(showBackground = true, name = "String Input Field")
@Composable
fun StringInputFieldPreview() {
    var text by remember { mutableStateOf("Test Text") }
    AichoPaichoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StringInputField(
                label = "Name",
                value = text,
                onValueChange = { text = it }
            )
        }
    }
}

@Composable
fun AmountInputField(
    label: String,
    onAmountTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    value: String
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onAmountTextChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f), // Subtle background on error
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Amount Input Field")
@Composable
fun AmountInputFieldPreview() {
    var amountInputText by remember { mutableStateOf("123") }
    val amountInt = amountInputText.toIntOrNull()
    val isError = amountInputText.isNotEmpty() && amountInt == null

    AichoPaichoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AmountInputField(
                label = "Transaction Amount",
                value = amountInputText,
                onAmountTextChange = { amountInputText = it },
                isError = isError,
                errorMessage = if (isError) "Please enter a valid number" else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    label: String,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    initializeWithCurrentDate: Boolean = false,
    selectedDate: Long?
) {
    var showDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate ?: if (initializeWithCurrentDate) System.currentTimeMillis() else null
    )

    val formattedDateText by remember(selectedDate) {
        derivedStateOf {
            selectedDate?.let {
                val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                dateFormatter.format(Date(it))
            } ?: ""
        }
    }

    LaunchedEffect(initializeWithCurrentDate, selectedDate) {
        if (initializeWithCurrentDate && selectedDate == null) {
            val now = System.currentTimeMillis()
            onDateSelected(now)
        }
        if (selectedDate != datePickerState.selectedDateMillis) {
             datePickerState.selectedDateMillis = selectedDate
        }
    }


    Column(modifier = modifier) {
        OutlinedTextField(
            value = formattedDateText,
            onValueChange = { },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { showDialog = true }),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }

    if (showDialog) {
        val dialogColors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            dayContentColor = MaterialTheme.colorScheme.onSurface,
            disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primaryContainer
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDateSelected(datePickerState.selectedDateMillis)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text( stringResource(R.string.cancel))
                }
            },
            colors = dialogColors
        ) {
            DatePicker(
                state = datePickerState,
                colors = dialogColors
            )
        }
    }
}


@Composable
fun MultiLineTextInputField(
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 3,
    maxLines: Int = 5,
    value: String
) {
     OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = (minLines * 24).dp),
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

@Preview(showBackground = true, name = "Multi-line Text Input Field")
@Composable
fun MultiLineTextInputFieldPreview() {
    var notes by remember { mutableStateOf("This is a note.\nIt can span multiple lines.") }
    AichoPaichoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MultiLineTextInputField(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it }
            )
            Text(
                text = "Notes: $notes",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
