package dev.nyxigale.aichopaicho.viewmodel.data

import dev.nyxigale.aichopaicho.data.entity.User


data class WelcomeScreenUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)