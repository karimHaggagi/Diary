package com.example.mongo.repository

import com.example.util.model.Diary
import com.example.util.Constants.APP_ID
import com.example.util.RequestState
import com.example.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
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
                        com.example.util.RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            })
                    }

            } catch (e: Exception) {
                flow {
                    emit(com.example.util.RequestState.Error(UserNotAuthenticatedException()))
                }
            }

        } else {
            flow {
                emit(com.example.util.RequestState.Error(UserNotAuthenticatedException()))
            }
        }
    }

    override suspend fun getSelectedDiary(diaryId: ObjectId): com.example.util.RequestState<Diary> {
        if (user != null) {
            try {
                val result = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                return com.example.util.RequestState.Success(data = result)
            } catch (e: Exception) {
                return com.example.util.RequestState.Error(e)
            }

        } else {
            return com.example.util.RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun addNewDiary(diary: Diary): com.example.util.RequestState<Diary> {
        return if (user != null) {
            realm.write {
                try {
                    val newDiary = copyToRealm(diary.apply { ownerId = user.id })
                    com.example.util.RequestState.Success(data = newDiary)
                } catch (e: Exception) {
                    com.example.util.RequestState.Error(e)
                }
            }

        } else {
            com.example.util.RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): com.example.util.RequestState<Diary> {
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
                    com.example.util.RequestState.Success(data = query)
                } catch (e: Exception) {
                    com.example.util.RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            com.example.util.RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(diaryId: ObjectId): com.example.util.RequestState<Unit> {
        return if (user != null) {
            realm.write {
                try {
                    val query =
                        query<Diary>(query = "_id == $0 AND ownerId ==$1", diaryId, user.id).find().first()
                    delete(query)
                    com.example.util.RequestState.Success(Unit)
                } catch (e: Exception) {
                    com.example.util.RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            com.example.util.RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllDiaries(): com.example.util.RequestState<Unit> {
        return if (user != null) {
            realm.write {
                try {
                    val query =
                        query<Diary>(query = "ownerId ==$0", user.id).find()
                    delete(query)
                    com.example.util.RequestState.Success(Unit)
                } catch (e: Exception) {
                    com.example.util.RequestState.Error(Exception("Diary not found"))
                }
            }
        } else {
            com.example.util.RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not logged in")