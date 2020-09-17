package com.like.drive.motorfeed.repository.user

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.like.drive.motorfeed.data.user.UserData
import java.io.File

interface UserRepository{
    suspend fun getUser(success: () -> Unit, fail: () -> Unit, userBan: () -> Unit, emptyUser: () -> Unit)
    suspend fun checkUser():Boolean
    suspend fun loginFaceBook(authCredential: AuthCredential, success:(FirebaseUser)->Unit, error:()->Unit)
    suspend fun setUser(userData: UserData,success:()->Unit,fail:()->Unit)
    suspend fun signEmail(email:String,password:String,success:(FirebaseUser)->Unit,error:()->Unit)
    suspend fun loginEmail(email:String,password:String,success:(FirebaseUser)->Unit,error:()->Unit)
    suspend fun updateProfile(nickName:String, imgFile: File?=null, intro:String?=null, existNickName: ()->Unit, success: (String?) -> Unit, fail: () -> Unit, notUser: () -> Unit)
    suspend fun signOut(success: () -> Unit,fail : ()->Unit)
    suspend fun resetPassword(email:String,success: () -> Unit,fail: () -> Unit)
    suspend fun updatePassword(password:String,success: () -> Unit,fail: () -> Unit)
}