package com.example.diaryapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.write.WriteScreen

@Composable
fun SetupNavigationGraph(startDestination: String, navHostController: NavHostController) {

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {

        authenticationRoute {
            navHostController.popBackStack()
            navHostController.navigateToHomeRoute()
        }
        homeRoute(navigateToAuthScreen = {
            navHostController.popBackStack()
            navHostController.navigateToAuthenticationRoute()
        }, navigateWriteScreen = {
            navHostController.navigateToWriteRoute(it)
        })
        writeRoute(onBackButtonPressed = {
            navHostController.popBackStack()
        })
    }

}


fun NavController.navigateToAuthenticationRoute(navOptions: NavOptions? = null) =
    navigate(Screens.Authentication.route, navOptions)

fun NavGraphBuilder.authenticationRoute(navigateToHomeScreen: () -> Unit) {

    composable(route = Screens.Authentication.route) {
        AuthenticationScreen(
            navigateToHomeScreen = navigateToHomeScreen
        )
    }
}


fun NavController.navigateToHomeRoute(navOptions: NavOptions? = null) =
    navigate(Screens.Home.route, navOptions)

fun NavGraphBuilder.homeRoute(
    navigateToAuthScreen: () -> Unit,
    navigateWriteScreen: (String?) -> Unit
) {

    composable(route = Screens.Home.route) {
        HomeScreen(
            navigateToAuthScreen = navigateToAuthScreen,
            navigateToWriteScreen = navigateWriteScreen
        )
    }
}


fun NavController.navigateToWriteRoute(diaryId: String?, navOptions: NavOptions? = null) =
    navigate(Screens.Write.passDiaryId(diaryId), navOptions)

fun NavGraphBuilder.writeRoute(onBackButtonPressed: () -> Unit) {

    composable(
        route = Screens.Write.route,
        arguments = listOf(
            navArgument(name = WRITE_SCREEN_ARGUMENT_DIARY_ID,
                builder = {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
        )
    ) {

        WriteScreen(onBackButtonPressed = onBackButtonPressed)
    }
}