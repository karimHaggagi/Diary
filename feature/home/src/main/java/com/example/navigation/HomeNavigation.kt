package com.example.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.util.Screens


fun NavController.navigateToHomeRoute(navOptions: NavOptions? = null) =
    navigate(Screens.Home.route, navOptions)

fun NavGraphBuilder.homeRoute(
    navigateToAuthScreen: () -> Unit,
    navigateWriteScreen: (String?) -> Unit
) {

    composable(route = Screens.Home.route) {
        com.example.home.HomeScreen(
            navigateToAuthScreen = navigateToAuthScreen,
            navigateToWriteScreen = navigateWriteScreen
        )
    }
}