package com.example.write.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.util.Screens
import com.example.util.WRITE_SCREEN_ARGUMENT_DIARY_ID
import com.example.write.WriteScreen


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