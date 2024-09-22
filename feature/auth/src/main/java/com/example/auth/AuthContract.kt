package com.example.auth

import com.example.ui.components.UiEffect
import com.example.ui.components.UiEvent
import com.example.ui.components.UiState

class AuthContract {

    sealed class AuthEvent : UiEvent {
        object OnLoadingStateChanged: AuthEvent()
        data class OnTokenReceived(val tokenID: String) : AuthEvent()
    }

    data class State(
        val loading: Boolean = false,
        val tokenID: String = ""
    ) : UiState


    sealed class AuthEffect : UiEffect {
        object navigateToHomeScreen : AuthEffect()
        object OnUserLoginSuccessfully : AuthEffect()
        data class OnUserLoginFailed(val message:String): AuthEffect()
    }
}