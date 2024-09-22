package com.example.home.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.components.DateHeader
import com.example.util.model.Diary
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiariesList(
    modifier: Modifier = Modifier,
    diaries: Map<LocalDate, List<Diary>>,
    onClick: (String) -> Unit = {}
) {

    LazyColumn(modifier = modifier.padding(all = 14.dp)) {
        diaries.forEach { (data, diariesDate) ->

            stickyHeader { DateHeader(localDate = data) }

            items(diariesDate, key = { it._id.toString() }) { item ->
                DiaryHolder(diary = item, onClick = onClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiariesListPreview() {
    DiariesList(
        diaries = mapOf(
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
}