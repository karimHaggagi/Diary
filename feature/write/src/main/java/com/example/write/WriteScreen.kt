package com.example.write

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.ZoomableImage
import com.example.util.model.Mood
import com.example.write.component.DiaryPager
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.rememberMessageBarState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WriteScreen(
    modifier: Modifier = Modifier,
    viewModel: WriteViewModel = viewModel(),
    onBackButtonPressed: () -> Unit = {}
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()

    val pagerState = rememberPagerState(pageCount = { state.images.size })

    val messageBarState = rememberMessageBarState()

    val multiplePhotoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { imagesUri ->
            viewModel.setEvent(WriteContract.WriteEvent.OnImagesChanged(imagesUri))
        }

    LaunchedEffect(key1 = state.selectedImage) {
        if (state.selectedImage != null) {
            val index = state.images.indexOf(state.selectedImage)
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(key1 = pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged() // Ensure that only unique changes are observed.
            .collect { page ->
                // Do something with each page change, for example:
                // viewModel.sendPageSelectedEvent(page)
                if(state.selectedImage!=null) {
                    viewModel.setEvent(WriteContract.WriteEvent.OnImageSelected(state.images[page]))
                }
            }
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
                Log.i("@@@state",pagerState.pageCount.toString())
                Box(
                    modifier = Modifier
                ) {
                DiaryPager(pagerState = pagerState) { page ->

                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.images[page])
                                .crossfade(true)
                                .build(),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Gallery Image"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            viewModel.setEvent(
                                WriteContract.WriteEvent.OnImageSelected(
                                    null
                                )
                            )
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Icon")
                            Text(text = "Close")
                        }
                        Button(onClick = {
                            viewModel.setEvent(
                                WriteContract.WriteEvent.OnImageDeleteClicked(
                                    state.selectedImage!!
                                )
                            )
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                            Text(text = "Delete")
                        }
                    }
                }
            }
        }
    }

}