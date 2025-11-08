package com.aspiring_creators.aichopaicho.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspiring_creators.aichopaicho.R
import com.aspiring_creators.aichopaicho.data.repository.ContactRepository
import com.aspiring_creators.aichopaicho.data.repository.RecordRepository
import com.aspiring_creators.aichopaicho.data.repository.TypeRepository
import com.aspiring_creators.aichopaicho.viewmodel.data.RecordDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordRepository: RecordRepository,
    private val contactRepository: ContactRepository,
    private val typeRepository: TypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val record = recordRepository.getRecordById(recordId)
                if (record != null) {
                    _uiState.value = _uiState.value.copy(record = record)

                    // Load contact and type details
                    record.contactId?.let { contactId ->
                        val contact = contactRepository.getContactById(contactId)
                        _uiState.value = _uiState.value.copy(contact = contact)
                    }

                    val type = typeRepository.getTypeById(record.typeId)
                    _uiState.value = _uiState.value.copy(type = type)
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = context.getString(R.string.record_not_found))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateAmount(amountString: String) {
        val amount = amountString.toIntOrNull() ?: 0
        _uiState.value.record?.let { record ->
            _uiState.value = _uiState.value.copy(
                record = record.copy(amount = amount, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun updateDescription(description: String) {
        _uiState.value.record?.let { record ->
            _uiState.value = _uiState.value.copy(
                record = record.copy(
                    description = description,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateDate(date: Long) {
        _uiState.value.record?.let { record ->
            _uiState.value = _uiState.value.copy(
                record = record.copy(date = date, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun toggleCompletion() {
        _uiState.value.record?.let { record ->
            val updatedRecord = record.copy(
                isComplete = !record.isComplete,
                updatedAt = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(record = updatedRecord)

            viewModelScope.launch {
                try {
                    recordRepository.updateRecord(updatedRecord)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun saveRecord() {
        _uiState.value.record?.let { record ->
            viewModelScope.launch {
                try {
                    recordRepository.updateRecord(record)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }

    fun deleteRecord() {
        _uiState.value.record?.let { record ->
            viewModelScope.launch {
                try {
                    recordRepository.deleteRecord(record.id)
                    _uiState.value = _uiState.value.copy(isRecordDeleted = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            }
        }
    }
    fun acknowledgeRecordDeleted()
    {
        _uiState.value = _uiState.value.copy(isRecordDeleted = false)
    }

    fun clearErrorMessage()
    {
        _uiState.value = _uiState.value.copy(errorMessage = null)

    }
}