package com.example.diaryapp.data.repository

import android.util.Log
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood
import com.example.diaryapp.utils.Constants.APP_ID
import com.example.diaryapp.utils.RequestState
import com.example.diaryapp.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId

object MongoDB : MongoDBRepository {

    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }


    override fun configureTheRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.id),
                        name = "User's Diaries"
                    )
                }
//                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)


        }
    }

    override suspend fun getAllDiaries(): Flow<Diaries> {

        return if (user != null) {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            })
                    }

            } catch (e: Exception) {
                flow {
                    emit(RequestState.Error(UserNotAuthenticatedException()))
                }
            }

        } else {
            flow {
                emit(RequestState.Error(UserNotAuthenticatedException()))
            }
        }
    }

    override suspend fun getSelectedDiary(diaryId: ObjectId): RequestState<Diary> {
        if (user != null) {
            try {
                val result = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                return RequestState.Success(data = result)
            } catch (e: Exception) {
                return RequestState.Error(e)
            }

        } else {
            return RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun addNewDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val newDiary = copyToRealm(diary.apply { ownerId = user.id })
                    RequestState.Success(data = newDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }

        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val query = query<Diary>(query = "_id == $0", diary._id).find().first()
                    query.apply {
                        title = diary.title
                        description = diary.description
                        mood = diary.mood
                        images = diary.images
                        date = diary.date
                    }
                    RequestState.Success(data = query)
                } catch (e: Exception) {
                    RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(diaryId: ObjectId): RequestState<Unit> {
        return if (user != null) {
            realm.write {
                try {
                    val query =
                        query<Diary>(query = "_id == $0 AND ownerId ==$1", diaryId, user.id).find().first()
                    delete(query)
                    RequestState.Success(Unit)
                } catch (e: Exception) {
                    RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllDiaries(): RequestState<Unit> {
        return if (user != null) {
            realm.write {
                try {
                    val query =
                        query<Diary>(query = "ownerId ==$0", user.id).find()
                    delete(query)
                    RequestState.Success(Unit)
                } catch (e: Exception) {
                    RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not logged in")