package com.example.diaryapp.presentation.screens.write

import android.net.Uri
import androidx.compose.runtime.Stable
import com.example.diaryapp.model.Mood
import com.example.diaryapp.presentation.components.UiEffect
import com.example.diaryapp.presentation.components.UiEvent
import com.example.diaryapp.presentation.components.UiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WriteContract {

    sealed class WriteEvent : UiEvent {
        data class OnTitleChanged(val title: String) : WriteEvent()
        data class OnDescriptionChanged(val description: String) : WriteEvent()
        data class OnMoodChanged(val mood: Mood) : WriteEvent()
        data class OnDateChanged(val date: LocalDate) : WriteEvent()
        data class OnTimeChanged(val time: LocalTime) : WriteEvent()
        data class OnImagesChanged(val images: List<Uri>) : WriteEvent()
        data class OnImageSelected(val image: Any?) : WriteEvent()
        data class OnImageDeleteClicked(val image: Any) : WriteEvent()
        object OnDateIconClick : WriteEvent()
        object OnSaveClicked : WriteEvent()
        object OnDeleteClicked : WriteEvent()
        object OnBackButtonPressed : WriteEvent()
        object OnAddImageButtonClicked : WriteEvent()
    }

    @Stable
    data class WriteState(
        val loading: Boolean = false,
        val mood: Mood = Mood.Happy,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        val title: String = "",
        val description: String = "",
        val selectedDiary: Boolean = false,
        val images: List<Any> = emptyList<String>(),
        val selectedImage: Any? = null
    ) : UiState {
        val formattedDate
            get() = DateTimeFormatter
                .ofPattern("dd MMM yyyy")
                .format(date).uppercase()

        val formattedTime
            get() = DateTimeFormatter
                .ofPattern("hh:mm a")
                .format(time).uppercase()
    }


    sealed class WriteEffect : UiEffect {
        data class DisplayMessage(val message: String) : WriteEffect()
        data class DisplayErrorMessage(val message: String) : WriteEffect()
        object OnBackButtonPressed : WriteEffect()
        object DisplayDatePicker : WriteEffect()
        object OpenGallery : WriteEffect()

    }
}
