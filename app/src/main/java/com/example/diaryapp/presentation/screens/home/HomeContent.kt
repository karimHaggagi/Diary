package com.example.diaryapp.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.model.Diary
import com.example.diaryapp.presentation.screens.home.component.DiariesList
import com.example.diaryapp.presentation.screens.home.component.EmptyPage
import com.example.diaryapp.presentation.screens.home.component.HomeTopAppBar
import com.example.diaryapp.presentation.screens.home.component.NavigationDrawerContent
import com.example.diaryapp.utils.RequestState
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    diaries: Diaries = RequestState.Idle,
    isDataFiltered: Boolean = false,
    onClick: (HomeContract.HomeEvent) -> Unit = {}
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(onSignOutClick = { onClick(HomeContract.HomeEvent.OnDisplayHideAlert) })
        },
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopAppBar(
                    isDataFiltered = isDataFiltered,
                    scrollBehavior = scrollBehavior,
                    onDrawerMenuIconClicked = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onDateMenuIconClicked = {
                        onClick(HomeContract.HomeEvent.OnDateIconClicked)

                    },
                    onDeleteMenuIconClickListener = {
                        onClick(HomeContract.HomeEvent.OnDeleteButtonClicked)
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { onClick(HomeContract.HomeEvent.OnDiaryItemClicked(null)) }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Icon")
                }
            },
            content = { paddingValues ->
                when (diaries) {
                    RequestState.Idle -> {}
                    RequestState.Loading -> {
                        Box(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    is RequestState.Success -> {
                        if (diaries.data.isEmpty()) {
                            EmptyPage()
                        } else {
                            DiariesList(
                                modifier = Modifier.padding(paddingValues),
                                diaries = diaries.data,
                                onClick = { onClick(HomeContract.HomeEvent.OnDiaryItemClicked(it)) })
                        }
                    }

                    is RequestState.Error -> {
                        EmptyPage(title = "Error!", subtitle = diaries.error.message ?: "")
                    }

                }
            }
        )
    }
}

@Preview
@Composable
private fun EmptyHomeContentPreview() {
    HomeContent()
}

@Preview
@Composable
private fun LoadingHomeContentPreview() {
    HomeContent(diaries = RequestState.Loading)
}

@Preview
@Composable
private fun HomeContentPreview() {
    HomeContent(
        diaries =
        RequestState.Success(
            mapOf(
                LocalDate.now() to listOf(
                    Diary().apply { title = "Diary #1" },
                    Diary().apply { title = "Diary #2" },
                    Diary().apply { title = "Diary #3" },
                    Diary().apply { title = "Diary #4" },
                ),
                LocalDate.now().minusDays(1) to listOf(
                    Diary().apply { title = "Diary #5" },
                    Diary().apply { title = "Diary #6" },
                    Diary().apply { title = "Diary #7" },
                    Diary().apply { title = "Diary #8" },
                )
            )
        )
    )
}
