package com.example.diaryapp.presentation.screens.write.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diaryapp.model.Mood

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoodPager(modifier: Modifier = Modifier, pagerState: PagerState) {

    HorizontalPager(
        modifier = modifier, state = pagerState
    ) { page ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(120.dp),
                painter = painterResource(id = Mood.entries[page].icon),
                contentDescription = "Mood Image"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun MoodPagerPreview() {
    val pagerState = rememberPagerState(pageCount = { Mood.entries.size })

    MoodPager(pagerState =  pagerState )
}