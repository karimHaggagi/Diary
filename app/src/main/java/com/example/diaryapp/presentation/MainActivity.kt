package com.example.diaryapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.util.Screens
import com.example.diaryapp.navigation.SetupNavigationGraph
import com.example.ui.theme.DiaryAppTheme
import com.example.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavigationGraph(
                    startDestination = getStartDestination(),
                    navHostController = navController
                )

                // MockedScreen()
            }
        }
    }

    private fun getStartDestination(): String {
        val currentUser= App.create(APP_ID).currentUser
        return if (currentUser!= null && currentUser.loggedIn) Screens.Home.route else Screens.Authentication.route
    }
}

