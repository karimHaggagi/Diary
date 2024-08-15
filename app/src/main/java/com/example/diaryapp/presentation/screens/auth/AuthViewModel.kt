package com.example.diaryapp.presentation.screens.auth

import androidx.lifecycle.viewModelScope
import com.example.diaryapp.presentation.components.BaseViewModel
import com.example.diaryapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel :
    BaseViewModel<AuthContract.State, AuthContract.AuthEvent, AuthContract.AuthEffect>() {
    override fun createInitialState(): AuthContract.State {
        return AuthContract.State()
    }

    override fun handleEvent(event: AuthContract.AuthEvent) {
        when (event) {
            is AuthContract.AuthEvent.OnTokenReceived -> {
                setState { copy(tokenID = event.tokenID) }
                signInWithMongoDBAtlas()
            }

            AuthContract.AuthEvent.OnLoadingStateChanged -> {
                setState { copy(loading = !currentState.loading) }
            }
        }
    }

    private fun signInWithMongoDBAtlas() {

        try {
            val credentials = GoogleAuthProvider.getCredential(currentState.tokenID, null)
            FirebaseAuth.getInstance().signInWithCredential(credentials)
                .addOnSuccessListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = App.Companion.create(Constants.APP_ID)
                            .login(
                                Credentials.jwt(currentState.tokenID)
                                //Credentials.google(currentState.tokenID, GoogleAuthType.ID_TOKEN)
                            ).loggedIn

                        if (result) {
                            setEffect(AuthContract.AuthEffect.OnUserLoginSuccessfully)
                            delay(600)
                            setEffect(AuthContract.AuthEffect.navigateToHomeScreen)
                        } else {
                            setEffect(AuthContract.AuthEffect.OnUserLoginFailed("Login Failed"))
                        }
                    }
                }.addOnFailureListener {
                    throw Exception(it.message)
                }

        } catch (e: Exception) {
            setEffect(AuthContract.AuthEffect.OnUserLoginFailed(e.message.toString()))
        }
        setState { copy(loading = false) }
    }
}
