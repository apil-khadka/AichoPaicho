package com.aspiring_creators.aichopaicho.viewmodel.data

import com.aspiring_creators.aichopaicho.data.entity.User


data class WelcomeScreenUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)