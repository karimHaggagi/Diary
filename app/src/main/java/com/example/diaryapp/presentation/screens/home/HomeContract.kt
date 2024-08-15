package com.example.diaryapp.presentation.screens.home

import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.model.Diary
import com.example.diaryapp.presentation.components.*
import com.example.diaryapp.utils.RequestState
import java.time.LocalDate

class HomeContract {

    sealed class HomeEvent : UiEvent {
        object OnDisplayHideAlert : HomeEvent()
        object OnSignOutButtonClicked : HomeEvent()
        object OnDeleteButtonClicked : HomeEvent()
        object OnConfirmDeleteButtonClicked : HomeEvent()
        object OnDateIconClicked : HomeEvent()
        data class OnDiaryItemClicked(val diaryId: String?) : HomeEvent()
        data class OnDateSelected(val date: LocalDate) : HomeEvent()
    }

    data class HomeState(
        val loading: Boolean = false,
        val isSignOutDialogDisplayed: Boolean = false,
        val isDeleteDiaryDialogDisplayed: Boolean = false,
        val diaries: Diaries = RequestState.Idle,
        val isDataFiltered: Boolean = false
    ) : UiState


    sealed class HomeEffect : UiEffect {
        object OnSignOutSuccessfully : HomeEffect()
        object DisplayDatePicker : HomeEffect()
        data class OnNavigateToWriteScreen(val diaryId: String?) : HomeEffect()
        data class DeleteSuccessfully(val message: String) : HomeEffect()
        data class DeleteFailed(val message: String) : HomeEffect()

    }
}