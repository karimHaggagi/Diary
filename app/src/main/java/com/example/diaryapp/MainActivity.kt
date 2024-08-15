package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.navigation.Screens
import com.example.diaryapp.navigation.SetupNavigationGraph
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.utils.Constants.APP_ID
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

