package com.example.diaryapp.presentation.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.*
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.write.WriteContract
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.rememberMessageBarState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    navigateToAuthScreen: () -> Unit = {},
    navigateToWriteScreen: (String?) -> Unit = {}
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageBarState = rememberMessageBarState()

    val dateDialog = rememberSheetState()

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { event ->
            when (event) {
                HomeContract.HomeEffect.OnSignOutSuccessfully -> {
                    navigateToAuthScreen()
                }

                is HomeContract.HomeEffect.OnNavigateToWriteScreen -> {
                    navigateToWriteScreen(event.diaryId)
                }

                is HomeContract.HomeEffect.DeleteFailed -> {
                    messageBarState.addError(Exception(event.message))
                }

                is HomeContract.HomeEffect.DeleteSuccessfully -> {
                    messageBarState.addSuccess(event.message)
                }

                HomeContract.HomeEffect.DisplayDatePicker -> {
                    dateDialog.show()
                }
            }
        }
    }

    ContentWithMessageBar(messageBarState = messageBarState) {

        HomeContent(
            modifier = modifier,
            isDataFiltered = state.isDataFiltered,
            diaries = state.diaries,
            onClick = viewModel::setEvent
        )

    }
    DisplayAlertDialog(
        title = "Sign Out",
        message = "Are you sure want to sign out?",
        dialogOpened = state.isSignOutDialogDisplayed,
        onDialogClosed = { viewModel.setEvent(HomeContract.HomeEvent.OnDisplayHideAlert) },
        onYesClicked = { viewModel.setEvent(HomeContract.HomeEvent.OnSignOutButtonClicked) })


    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure want to delete all diaries?",
        dialogOpened = state.isDeleteDiaryDialogDisplayed,
        onDialogClosed = { viewModel.setEvent(HomeContract.HomeEvent.OnDeleteButtonClicked) },
        onYesClicked = { viewModel.setEvent(HomeContract.HomeEvent.OnConfirmDeleteButtonClicked) })


    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate ->
            viewModel.setEvent(HomeContract.HomeEvent.OnDateSelected(localDate))
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
}