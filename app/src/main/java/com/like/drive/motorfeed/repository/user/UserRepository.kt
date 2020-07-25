package com.like.drive.motorfeed.repository.user

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.like.drive.motorfeed.common.async.ResultState
import com.like.drive.motorfeed.data.user.UserData

interface UserRepository{
    suspend fun getUser(success: () -> Unit, fail: () -> Unit, ban: () -> Unit, empty: () -> Unit)
    suspend fun checkUser():Boolean
    suspend fun loginFaceBook(authCredential: AuthCredential, success:(FirebaseUser)->Unit, error:()->Unit)
    suspend fun setUser(userData: UserData,success:()->Unit,fail:()->Unit)
    suspend fun signEmail(email:String,password:String,success:(FirebaseUser)->Unit,error:()->Unit)
    suspend fun loginEmail(email:String,password:String,success:(FirebaseUser)->Unit,error:()->Unit)
    suspend fun signOut(success: () -> Unit,fail : ()->Unit)
}