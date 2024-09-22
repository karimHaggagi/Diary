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
import com.example.write.WriteScreen
import com.example.navigation.authenticationRoute
import com.example.navigation.homeRoute
import com.example.navigation.navigateToAuthenticationRoute
import com.example.navigation.navigateToHomeRoute
import com.example.util.Screens
import com.example.util.WRITE_SCREEN_ARGUMENT_DIARY_ID
import com.example.write.navigation.navigateToWriteRoute
import com.example.write.navigation.writeRoute

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