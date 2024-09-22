package com.example.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.util.Screens

fun NavController.navigateToAuthenticationRoute(navOptions: NavOptions? = null) =
    navigate(Screens.Authentication.route, navOptions)

fun NavGraphBuilder.authenticationRoute(navigateToHomeScreen: () -> Unit) {

    composable(route = Screens.Authentication.route) {
        com.example.auth.AuthenticationScreen(
            navigateToHomeScreen = navigateToHomeScreen
        )
    }
}
