package com.example.diaryapp.presentation.screens.write

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood
import com.example.diaryapp.navigation.WRITE_SCREEN_ARGUMENT_DIARY_ID
import com.example.diaryapp.presentation.components.BaseViewModel
import com.example.diaryapp.utils.RequestState
import com.example.diaryapp.utils.toInstant
import com.example.diaryapp.utils.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WriteViewModel(private val savedStateHandle: SavedStateHandle) :
    BaseViewModel<WriteContract.WriteState, WriteContract.WriteEvent, WriteContract.WriteEffect>() {

    private var diaryId: String? = null
    private val selectedImages = mutableListOf<Uri>()
    private val deletedImages = mutableListOf<String>()

    override fun createInitialState(): WriteContract.WriteState {
        return WriteContract.WriteState()
    }

    init {
        checkDiaryID()
    }

    private fun checkDiaryID() {
        diaryId = savedStateHandle.get<String>(WRITE_SCREEN_ARGUMENT_DIARY_ID)

        viewModelScope.launch(Dispatchers.IO) {
            if (diaryId != null) {
                setState { copy(loading = true, selectedDiary = true) }
                delay(3000)

                when (val result = MongoDB.getSelectedDiary(ObjectId.invoke(diaryId!!))) {
                    is RequestState.Error -> {}
                    RequestState.Idle -> {}
                    RequestState.Loading -> {}
                    is RequestState.Success -> {
                        setState {
                            copy(
                                loading = false,
                                selectedDiary = true,
                                title = result.data.title,
                                description = result.data.description,
                                mood = Mood.valueOf(result.data.mood),
                                date = result.data.date.toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                                time = result.data.date.toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalTime(),
                                images = result.data.images
                            )
                        }
                    }
                }
            }
        }
    }

    override fun handleEvent(event: WriteContract.WriteEvent) {
        when (event) {
            is WriteContract.WriteEvent.OnDateChanged -> {
                setState { copy(date = event.date) }
            }

            WriteContract.WriteEvent.OnDeleteClicked -> {
                deleteDiary()
            }

            is WriteContract.WriteEvent.OnDescriptionChanged -> {
                setState { copy(description = event.description) }
            }

            is WriteContract.WriteEvent.OnMoodChanged -> {
                setState { copy(mood = event.mood) }
            }

            WriteContract.WriteEvent.OnSaveClicked -> {
                if (currentState.selectedDiary) {
                    updateCurrentDiary()
                } else {
                    addNewDiary()
                }
            }

            is WriteContract.WriteEvent.OnTimeChanged -> {
                setState { copy(time = event.time) }
            }

            is WriteContract.WriteEvent.OnTitleChanged -> {
                setState { copy(title = event.title) }
            }

            WriteContract.WriteEvent.OnBackButtonPressed -> {
                setEffect(WriteContract.WriteEffect.OnBackButtonPressed)
            }

            WriteContract.WriteEvent.OnDateIconClick -> {
                setEffect(WriteContract.WriteEffect.DisplayDatePicker)
            }

            WriteContract.WriteEvent.OnAddImageButtonClicked -> {
                setEffect(WriteContract.WriteEffect.OpenGallery)
            }

            is WriteContract.WriteEvent.OnImagesChanged -> {
                selectedImages.addAll(event.images)
                val images = currentState.images.toMutableList().apply { addAll(0, event.images) }
                setState { copy(images = images) }
            }

            is WriteContract.WriteEvent.OnImageSelected -> {
                setState { copy(selectedImage = event.image) }
            }

            is WriteContract.WriteEvent.OnImageDeleteClicked -> {
                selectedImages.remove(event.image)
                if (event.image is String) {
                    deletedImages.add(event.image)
                }
                val images = currentState.images.toMutableList().apply { remove(event.image) }
                setState { copy(images = images, selectedImage = null) }

            }
        }
    }

    private fun deleteFirebaseImages(deleteAll: Boolean = true) {
        val images = if (deleteAll) currentState.images.filterIsInstance<String>()
        else deletedImages

        val storage = FirebaseStorage.getInstance()

        images.forEach {
            storage.getReferenceFromUrl(it).delete()
        }
    }

    private fun deleteDiary() {
        viewModelScope.launch(Dispatchers.IO) {
            if (diaryId != null) {
                setState { copy(loading = true) }
                when (val result = MongoDB.deleteDiary(ObjectId.invoke(diaryId!!))) {
                    RequestState.Idle -> {}
                    RequestState.Loading -> {}
                    is RequestState.Success -> {
                        setState { copy(loading = false) }
                        deleteFirebaseImages()

                        setEffect(WriteContract.WriteEffect.DisplayMessage("Diary Deleted Successfully"))
                        delay(600)
                        setEffect(WriteContract.WriteEffect.OnBackButtonPressed)
                    }

                    is RequestState.Error -> {
                        setState { copy(loading = false) }
                        setEffect(
                            WriteContract.WriteEffect.DisplayErrorMessage(
                                result.error.message ?: "Unknown error"
                            )
                        )
                    }

                }
            }
        }
    }

    private fun updateCurrentDiary() {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentState.title.isBlank() || currentState.description.isBlank()) {
                setEffect(WriteContract.WriteEffect.DisplayErrorMessage("Fields can't be empty"))
            } else {
                setState { copy(loading = true) }
                val instant = LocalDateTime.of(currentState.date, currentState.time)
                    .atZone(ZoneId.systemDefault()) // Use your desired time zone
                    .toInstant()

                val uploadedImages = addImagesToFirebase()
                val currentImages = currentState.images.filterIsInstance<String>()
                val allImages = currentImages + uploadedImages

                deleteFirebaseImages(deleteAll = false)

                val diary = Diary().apply {
                    _id = ObjectId.invoke(diaryId!!)
                    title = currentState.title
                    description = currentState.description
                    mood = currentState.mood.name
                    date = instant.toRealmInstant()
                    images = allImages.toRealmList()
                }
                when (val result = MongoDB.updateDiary(diary)) {
                    RequestState.Idle -> {}
                    RequestState.Loading -> {}
                    is RequestState.Success -> {
                        setState { copy(loading = false) }
                        setEffect(WriteContract.WriteEffect.DisplayMessage("Diary Updated Successfully"))
                        delay(600)
                        setEffect(WriteContract.WriteEffect.OnBackButtonPressed)

                    }

                    is RequestState.Error -> {
                        setState { copy(loading = false) }
                        setEffect(
                            WriteContract.WriteEffect.DisplayErrorMessage(
                                result.error.message ?: "Unknown error"
                            )
                        )
                    }

                }
            }
        }
    }

    private fun getImagePath(imageUri: Uri): String {
        val imagePath =
            "images/${FirebaseAuth.getInstance().currentUser?.uid}/${imageUri.lastPathSegment}-${System.currentTimeMillis()}.jpg"
        return imagePath
    }

    private suspend fun addImagesToFirebase(): List<String> = suspendCoroutine { continuation ->

        val imagesUrl = mutableListOf<String>()
        val storage = FirebaseStorage.getInstance().reference
        if (selectedImages.isEmpty()){
            continuation.resume(imagesUrl)
        }
        selectedImages.forEachIndexed { index, imageUri ->
            val imagePath = getImagePath(imageUri)
            val imageRef = storage.child(imagePath)
            imageRef.putFile(imageUri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.storage.downloadUrl.addOnSuccessListener { url ->

                        imagesUrl.add("$url")
                        if (selectedImages.size == imagesUrl.size) {
                            continuation.resume(imagesUrl)
                        }
                    }

                } else {
                    throw Exception(task.exception)
                }
            }
        }

    }

    private fun addNewDiary() {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentState.title.isBlank() || currentState.description.isBlank()) {
                setEffect(WriteContract.WriteEffect.DisplayErrorMessage("Fields can't be empty"))
            } else {
                setState { copy(loading = true) }

                val uploadedImages = addImagesToFirebase()

                val instant = LocalDateTime.of(currentState.date, currentState.time)
                    .atZone(ZoneId.systemDefault()) // Use your desired time zone
                    .toInstant()

                val diary = Diary().apply {
                    title = currentState.title
                    description = currentState.description
                    mood = currentState.mood.name
                    date = instant.toRealmInstant()
                    images = uploadedImages.map { it }.toRealmList()
                }
                when (val result = MongoDB.addNewDiary(diary)) {
                    RequestState.Idle -> {}
                    RequestState.Loading -> {}
                    is RequestState.Success -> {
                        setState { copy(loading = false) }
                        setEffect(WriteContract.WriteEffect.DisplayMessage("Diary Added Successfully"))
                        delay(600)
                        setEffect(WriteContract.WriteEffect.OnBackButtonPressed)

                    }

                    is RequestState.Error -> {
                        setState { copy(loading = false) }
                        setEffect(
                            WriteContract.WriteEffect.DisplayErrorMessage(
                                result.error.message ?: "Unknown error"
                            )
                        )
                    }
                }
            }
        }
    }
}