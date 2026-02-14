package com.aspiring_creators.aichopaicho.viewmodel.data

data class PermissionScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val permissionGranted: Boolean = false
)