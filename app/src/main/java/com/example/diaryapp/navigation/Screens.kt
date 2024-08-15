package com.example.diaryapp.navigation

const val WRITE_SCREEN_ARGUMENT_DIARY_ID = "diaryId"

sealed class Screens(val route: String) {
    data object Authentication : Screens("authentication_screen")
    data object Home : Screens("hme_screen")
    object Write :
        Screens("write_screen?$WRITE_SCREEN_ARGUMENT_DIARY_ID={$WRITE_SCREEN_ARGUMENT_DIARY_ID}") {
        fun passDiaryId(diaryId: String?): String {
            return "write_screen?$WRITE_SCREEN_ARGUMENT_DIARY_ID=${diaryId}"
        }
    }
}