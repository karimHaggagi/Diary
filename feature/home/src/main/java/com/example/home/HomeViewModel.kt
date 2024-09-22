package com.example.home

import androidx.lifecycle.viewModelScope
import com.example.mongo.repository.MongoDB
import com.example.util.model.Diary
import com.example.ui.components.BaseViewModel
import com.example.util.Constants.APP_ID
import com.example.util.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import kotlin.coroutines.resume

internal class HomeViewModel :
    BaseViewModel<HomeContract.HomeState, HomeContract.HomeEvent, HomeContract.HomeEffect>() {

    init {
        getAllDiaries()
    }

    private var currentList: Map<LocalDate, List<Diary>> = mapOf()

    private fun getAllDiaries() {
        viewModelScope.launch(Dispatchers.IO) {
            setState { copy(diaries = RequestState.Loading) }
            MongoDB.getAllDiaries().collect { result ->
                setState { copy(diaries = result) }
                if (result is RequestState.Success) {
                    currentList = result.data
                }
            }
        }
    }

    override fun createInitialState(): HomeContract.HomeState {
        return HomeContract.HomeState()
    }

    override fun handleEvent(event: HomeContract.HomeEvent) {
        when (event) {
            HomeContract.HomeEvent.OnDisplayHideAlert -> {
                setState { copy(isSignOutDialogDisplayed = !currentState.isSignOutDialogDisplayed) }
            }

            HomeContract.HomeEvent.OnSignOutButtonClicked -> {
                signOut()
            }

            is HomeContract.HomeEvent.OnDiaryItemClicked -> {
                setEffect(HomeContract.HomeEffect.OnNavigateToWriteScreen(event.diaryId))
            }

            HomeContract.HomeEvent.OnConfirmDeleteButtonClicked -> {
                deleteAll()
            }

            HomeContract.HomeEvent.OnDeleteButtonClicked -> {
                setState { copy(isDeleteDiaryDialogDisplayed = !currentState.isDeleteDiaryDialogDisplayed) }
            }

            HomeContract.HomeEvent.OnDateIconClicked -> {
                if (!currentState.isDataFiltered) {
                    setEffect(HomeContract.HomeEffect.DisplayDatePicker)
                } else {
                    setState {
                        copy(
                            diaries = RequestState.Success(currentList),
                            isDataFiltered = false
                        )
                    }

                }
            }

            is HomeContract.HomeEvent.OnDateSelected -> {
                filterResult(event.date)
            }
        }
    }

    private fun filterResult(date: LocalDate) {
        val filteredList = currentList.filter { it.key == date }
        setState { copy(diaries = RequestState.Success(filteredList), isDataFiltered = true) }
    }

    private suspend fun deleteImagesFromFirebase() = suspendCancellableCoroutine { result ->
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val imageDirectory = "images/${currentUser}"
        val storage = FirebaseStorage.getInstance().reference
        storage.child(imageDirectory)
            .listAll().addOnSuccessListener { ref ->
                ref.items.forEach { item ->
                    val path = "images/${currentUser}/${item.name}"
                    storage.child(path).delete()
                }
                result.resume(true)
            }.addOnFailureListener {
                result.resume(false)
            }
    }

    private fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            setState { copy(diaries = RequestState.Loading) }
            if (!deleteImagesFromFirebase()) {
                setEffect(HomeContract.HomeEffect.DeleteFailed("Failed to delete images"))
                getAllDiaries()
                return@launch
            }
            when (MongoDB.deleteAllDiaries()) {
                RequestState.Idle -> {}
                RequestState.Loading -> {
                }

                is RequestState.Success -> {
                    getAllDiaries()
                }

                is RequestState.Error -> {}
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                App.create(APP_ID).currentUser?.logOut()
                setEffect(HomeContract.HomeEffect.OnSignOutSuccessfully)
            } catch (e: Exception) {
            }
        }
    }
}