package com.like.drive.motorfeed.remote.common

import android.net.Uri
import bolts.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream


class FireBaseTask {
    suspend fun <C> getData(doc: DocumentReference, objectClass: Class<C>): Flow<C?> =
        flow {
            val snapShot = doc.get().await()
            if (snapShot.exists())
                emit(snapShot.toObject(objectClass))
            else
                emit(null)
        }


    suspend fun <R, M> getData(ref: R, objectClass: Class<M>): Flow<List<M>> =
        flow {
            when (ref) {
                is CollectionReference -> {
                    val snapShot = ref.get().await()
                    val result = snapShot.toObjects(objectClass)
                    emit(result)
                }

                is Query -> {
                    val snapShot = ref.get().await()
                    val result = snapShot.toObjects(objectClass)
                    emit(result)

                }

                else -> emit(emptyList())
            }
        }


    suspend fun <R> setData(ref: R, data: Any): Flow<Boolean> =
        flow {
            when (ref) {
                is DocumentReference -> {
                    val snapShot = ref.set(data).await()
                    emit(Task.forResult(snapShot).isCompleted)
                }

                is CollectionReference -> {
                    val snapShot = ref.add(data).await()
                    emit(Task.forResult(snapShot).isCompleted)
                }


                else -> emit(false)
            }
        }

    suspend fun updateData(
        ref: DocumentReference,
        date: Any,
        fieldName: String
    ): Flow<Boolean> =
        flow {
            val snapShot = ref.update(fieldName, date).await()
            emit(Tasks.forResult(snapShot).isComplete)
        }

    suspend fun delete(ref: DocumentReference): Flow<Boolean> =
        flow {
            val snapShot = ref.delete().await()
            emit(Tasks.forResult(snapShot).isComplete)
        }


    suspend fun uploadImage(ref: StorageReference, file: File): Flow<Uri?> =
        flow {
            val uploadTask = ref.putStream(FileInputStream(file)).await()
            if (uploadTask.task.isComplete) {
                emit(uploadTask.storage.downloadUrl.await())
            } else {
                emit(null)
            }
        }


}
