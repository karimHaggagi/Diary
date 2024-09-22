package com.example.mongo.repository

import com.example.util.model.Diary
import com.example.util.RequestState
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