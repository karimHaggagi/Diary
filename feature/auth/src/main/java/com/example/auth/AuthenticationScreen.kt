package com.example.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.util.Constants
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.stevdzasan.onetap.rememberOneTapSignInState
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    navigateToHomeScreen: () -> Unit
) {

    val oneTabState = rememberOneTapSignInState()
    val messageBarState = rememberMessageBarState()

    val currentState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthContract.AuthEffect.OnUserLoginFailed -> {
                    messageBarState.addError(Exception(effect.message))

                }

                AuthContract.AuthEffect.OnUserLoginSuccessfully -> {
                    messageBarState.addSuccess("Login Successfully!")

                }

                AuthContract.AuthEffect.navigateToHomeScreen -> {
                    navigateToHomeScreen()
                }
            }
        }
    }

    Scaffold(modifier = modifier) {
        ContentWithMessageBar(messageBarState = messageBarState) {
            AuthenticationContent(
                loadingState = currentState.loading,
                onButtonClicked = {
                    oneTabState.open()
                    viewModel.setEvent(AuthContract.AuthEvent.OnLoadingStateChanged)
                }
            )
        }
    }
    OneTapSignInWithGoogle(
        state = oneTabState,
        clientId = com.example.util.Constants.CLIENT_ID,
        rememberAccount = false,
        onTokenIdReceived = { token ->
            Log.i("@@token", token)
            viewModel.setEvent(AuthContract.AuthEvent.OnTokenReceived(token))
        },
        onDialogDismissed = { exception ->
            viewModel.setEvent(AuthContract.AuthEvent.OnLoadingStateChanged)
            viewModel.setEffect(AuthContract.AuthEffect.OnUserLoginFailed(exception))
        }
    )
}
