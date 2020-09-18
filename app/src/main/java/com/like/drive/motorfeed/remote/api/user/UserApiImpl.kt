package com.like.drive.motorfeed.remote.api.user

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.like.drive.motorfeed.common.user.UserInfo
import com.like.drive.motorfeed.data.user.UserData
import com.like.drive.motorfeed.remote.common.FireBaseTask
import com.like.drive.motorfeed.remote.reference.CollectionName.USER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserApiImpl(
    private val fireBaseTask: FireBaseTask,
    private val fireStore: FirebaseFirestore,
    private val fireAuth: FirebaseAuth
) : UserApi {
    override suspend fun getUser(): Flow<UserData?> {
        return fireBaseTask.getData(
            fireStore.collection(USER).document(fireAuth.uid!!),
            UserData::class.java
        )
    }

    override suspend fun checkUser(): Boolean {
        return fireAuth.currentUser != null
    }

    override suspend fun loginFacebook(authCredential: AuthCredential): Flow<AuthResult> =
        flow { emit(fireAuth.signInWithCredential(authCredential).await()) }

    override suspend fun setUser(userData: UserData): Flow<Boolean> {
        return fireBaseTask.setData(fireStore.collection(USER).document(fireAuth.uid!!), userData)
    }

    override suspend fun signEmail(email: String, password: String): Flow<AuthResult> =
        flow { emit(fireAuth.createUserWithEmailAndPassword(email, password).await()) }

    override suspend fun loginEmail(email: String, password: String): Flow<AuthResult> =
        flow { emit(fireAuth.signInWithEmailAndPassword(email, password).await()) }

    override suspend fun signOut() {
        fireAuth.currentUser?.let {
            fireAuth.signOut()
        }
    }

    override suspend fun setUserProfile(
        uid: String,
        nickName: String,
        imgPath: String?,
        intro: String?
    ): Flow<Boolean> {
        val document = fireStore.collection(USER).document(uid)

        val map = mutableMapOf("nickName" to nickName)
        imgPath?.let {
            map.put("profileImgPath", it)
        }
        intro?.let {
            map.put("intro", it)
        }

        return fireBaseTask.updateData(document, map)
    }

    override suspend fun checkNickName(nickName: String): Flow<List<UserData>> {
        val query = fireStore.collection(USER).whereEqualTo(NICK_NAME_FIELD, nickName)
        return fireBaseTask.getData(query, UserData::class.java)
    }

    override suspend fun updateFcmToken(token: String): Flow<Boolean> {
        return UserInfo.userInfo?.uid?.let {
            val document = fireStore.collection(USER).document(it)
            val map = mapOf("fcmToken" to token)

            fireBaseTask.updateData(document, map)
        } ?: emptyFlow()
    }

    override suspend fun updateCommentSubscribe(isSubscribe: Boolean): Flow<Boolean> =
        flow {
            UserInfo.userInfo?.uid?.let {
                val document = fireStore.collection(USER).document(it)
                val map = mapOf("isCommentSubscribe" to isSubscribe)

                return@let (fireBaseTask.updateData(document, map))
            } ?: emit(false)
        }

    override suspend fun resetPassword(email: String): Flow<Boolean> {
        return flow {
            val snapShot = fireAuth.sendPasswordResetEmail(email).await()
            emit(Tasks.forResult(snapShot).isSuccessful)
        }
    }

    override suspend fun updatePassword(password: String): Flow<Boolean> {
        return flow {
            fireAuth.currentUser?.let {
                val snapShot = it.updatePassword(password).await()
                emit(Tasks.forResult(snapShot).isSuccessful)
            } ?: emit(false)
        }
    }

    override suspend fun checkCredential(password: String): Flow<Boolean> {
        return flow {

            val credential =
                EmailAuthProvider.getCredential(UserInfo.userInfo?.email!!, password)

            fireAuth.currentUser?.run {
                val snapShot = reauthenticate(credential).await()
                emit(Tasks.forResult(snapShot).isComplete)
            }

        }
    }

    companion object {
        const val NICK_NAME_FIELD = "nickName"
    }
}
