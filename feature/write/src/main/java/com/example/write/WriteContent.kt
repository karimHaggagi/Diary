package com.example.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.util.model.Mood
import com.example.ui.components.Gallery
import com.example.ui.components.PlusOverlay
import com.example.write.component.DiaryPager
import com.example.write.component.WriteTopBar
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WriteContent(
    modifier: Modifier = Modifier,
    state: WriteContract.WriteState,
    onEventChanged: (WriteContract.WriteEvent) -> Unit
) {

    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { Mood.entries.size })

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }

    LaunchedEffect(key1 = pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged() // Ensure that only unique changes are observed.
            .collect { page ->
                // Do something with each page change, for example:
                // viewModel.sendPageSelectedEvent(page)
                onEventChanged(WriteContract.WriteEvent.OnMoodChanged(Mood.entries[page]))
            }
    }
    LaunchedEffect(key1 = state.mood, block = {
        val pageIndex = Mood.entries.indexOf(state.mood)
        if (pagerState.currentPage != pageIndex) {
            pagerState.scrollToPage(pageIndex)
        }
    })

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            WriteTopBar(
                moodName = state.mood.name,
                date = state.formattedDate,
                time = state.formattedTime,
                selectedDiary = state.selectedDiary,
                onEventChanged = onEventChanged
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState)
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                DiaryPager(pagerState = pagerState, pageContent = { page ->
                    Image(
                        modifier = Modifier.size(120.dp),
                        painter = painterResource(id = Mood.entries[page].icon),
                        contentDescription = "Mood Image"
                    )
                })

                Spacer(modifier = Modifier.height(30.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester1),
                    value = state.title,
                    placeholder = { Text(text = "Title") },
                    onValueChange = { onEventChanged(WriteContract.WriteEvent.OnTitleChanged(it)) },
                    maxLines = 1,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusRequester2.requestFocus()
                        }
                    )
                )


                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester2),
                    value = state.description,
                    placeholder = { Text(text = "Description") },

                    onValueChange = {
                        onEventChanged(
                            WriteContract.WriteEvent.OnDescriptionChanged(
                                it
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequester2.freeFocus()
                        }
                    ),
                )


            }

            Column(
                modifier = Modifier.padding(all = 14.dp),
                verticalArrangement = Arrangement.Bottom
            ) {

                Row(modifier = Modifier.fillMaxWidth()) {
                    PlusOverlay(onClick = { onEventChanged(WriteContract.WriteEvent.OnAddImageButtonClicked) })
                    Spacer(modifier = Modifier.width(10.dp))
                    if (state.images.isNotEmpty()) {
                        Gallery(
                            images = state.images,
                            onImageClicked = {
                                onEventChanged(
                                    WriteContract.WriteEvent.OnImageSelected(it)
                                )
                            })
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = {
                        onEventChanged(
                            WriteContract.WriteEvent.OnSaveClicked
                        )
                    },
                    shape = Shapes().small
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}


@Preview
@Composable
private fun WriteContentPreview() {
    WriteContent(state = WriteContract.WriteState(), onEventChanged = {})
}