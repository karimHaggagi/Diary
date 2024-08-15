package com.example.diaryapp.presentation.screens.write

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.*
import com.example.diaryapp.presentation.components.ZoomableImage
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.rememberMessageBarState
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    modifier: Modifier = Modifier,
    viewModel: WriteViewModel = viewModel(),
    onBackButtonPressed: () -> Unit = {}
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()

    val messageBarState = rememberMessageBarState()

    val multiplePhotoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { imagesUri ->
            viewModel.setEvent(WriteContract.WriteEvent.OnImagesChanged(imagesUri))
        }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                WriteContract.WriteEffect.OnBackButtonPressed -> {
                    onBackButtonPressed()
                }

                WriteContract.WriteEffect.DisplayDatePicker -> {
                    dateDialog.show()
                }

                is WriteContract.WriteEffect.DisplayErrorMessage -> {
                    messageBarState.addError(Exception(effect.message))
                }

                is WriteContract.WriteEffect.DisplayMessage -> {
                    messageBarState.addSuccess(effect.message)
                }

                WriteContract.WriteEffect.OpenGallery -> {
                    multiplePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
    }

    ContentWithMessageBar(messageBarState = messageBarState) {
        WriteContent(modifier = modifier, state = state, onEventChanged = viewModel::setEvent)
    }
    AnimatedVisibility(
        visible = state.loading,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.5f))
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate ->
            viewModel.setEvent(WriteContract.WriteEvent.OnDateChanged(localDate))
            timeDialog.show()
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )

    ClockDialog(
        state = timeDialog,
        selection = ClockSelection.HoursMinutes { hours, minutes ->
            viewModel.setEvent(
                WriteContract.WriteEvent.OnTimeChanged(
                    LocalTime.of(
                        hours,
                        minutes
                    )
                )
            )
        }
    )

    AnimatedVisibility(
        visible = state.selectedImage != null,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Dialog(onDismissRequest = { viewModel.setEvent(WriteContract.WriteEvent.OnImageSelected(null)) }) {
            if (state.selectedImage != null) {
                ZoomableImage(
                    selectedGalleryImage = state.selectedImage!!,
                    onCloseClicked = {
                        viewModel.setEvent(
                            WriteContract.WriteEvent.OnImageSelected(
                                null
                            )
                        )
                    },
                    onDeleteClicked = {
                        viewModel.setEvent(
                            WriteContract.WriteEvent.OnImageDeleteClicked(
                                state.selectedImage!!
                            )
                        )
                    })
            }
        }
    }

}