package com.example.diaryapp.data.repository

import com.example.diaryapp.model.Diary
import com.example.diaryapp.utils.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoDBRepository {
    fun configureTheRealm()
    suspend fun getAllDiaries(): Flow<Diaries>
    suspend fun getSelectedDiary(diaryId: ObjectId): RequestState<Diary>
    suspend fun addNewDiary(diary: Diary): RequestState<Diary>
    suspend fun updateDiary(diary: Diary): RequestState<Diary>
    suspend fun deleteDiary(diaryId: ObjectId): RequestState<Unit>
    suspend fun deleteAllDiaries(): RequestState<Unit>
}