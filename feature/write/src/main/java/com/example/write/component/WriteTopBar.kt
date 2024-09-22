package com.example.write.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.util.model.Mood
import com.example.write.WriteContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    moodName: String = Mood.Happy.name,
    date: String = "",
    time: String = "",
    selectedDiary: Boolean = false,
    onEventChanged: (WriteContract.WriteEvent) -> Unit
) {

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = { onEventChanged(WriteContract.WriteEvent.OnBackButtonPressed) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Arrow Icon"
                )
            }
        },
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = moodName,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    text = "$date, $time",
                    style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            IconButton(onClick = { onEventChanged(WriteContract.WriteEvent.OnDateIconClick) }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if (selectedDiary) {
                DeleteDiaryAction(
                    onDeleteConfirmed = { onEventChanged(WriteContract.WriteEvent.OnDeleteClicked) }
                )
            }
        }
    )

}

@Preview
@Composable
private fun WriteTopBarPreview() {
    WriteTopBar(
        date = WriteContract.WriteState().formattedDate,
        time = WriteContract.WriteState().formattedTime,
        onEventChanged = {})
}


