package dev.nyxigale.aichopaicho.viewmodel.data

sealed class WelcomeScreenUiEvent {
    object OnLoginSuccess : WelcomeScreenUiEvent()
    object OnSkipSuccess : WelcomeScreenUiEvent()
}