package com.like.drive.motorfeed.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.like.drive.motorfeed.data.notification.NotificationSendData
import java.util.*

@Entity
class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val mID: Int? = null,
    val notificationType: String? = null,
    val gid: String? = null,
    val eid: String? = null,
    val fid: String? = null,
    val uid: String? = null,
    val title:String?=null,
    val message: String? = null,
    val createData: Date?=null
) {
    fun dataToEntity(data: NotificationSendData) = NotificationEntity(
        notificationType = data.notificationType,
        gid = data.gid,
        eid = data.eid,
        fid = data.fid,
        uid = data.uid,
        title = data.title,
        message = data.message,
        createData = Date()
    )

}