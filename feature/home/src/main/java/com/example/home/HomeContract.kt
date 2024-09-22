package com.example.home

import com.example.ui.components.UiEffect
import com.example.ui.components.UiEvent
import com.example.ui.components.UiState
import java.time.LocalDate

internal class HomeContract {

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
        val diaries: com.example.mongo.repository.Diaries = com.example.util.RequestState.Idle,
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