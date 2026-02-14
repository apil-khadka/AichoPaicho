package com.aspiring_creators.aichopaicho.viewmodel.data

sealed class WelcomeScreenUiEvent {
    object OnLoginSuccess : WelcomeScreenUiEvent()
    object OnSkipSuccess : WelcomeScreenUiEvent()
}