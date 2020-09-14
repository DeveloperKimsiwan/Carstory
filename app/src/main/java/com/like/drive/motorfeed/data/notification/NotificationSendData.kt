package com.like.drive.motorfeed.data.notification

import com.google.gson.annotations.SerializedName
import com.like.drive.motorfeed.MotorFeedApplication
import com.like.drive.motorfeed.R
import com.like.drive.motorfeed.cache.entity.NotificationEntity
import java.util.*

data class NotificationSendData(
    @SerializedName("notificationType")
    val notificationType: String? = null,
    @SerializedName("gid")
    val gid: String? = null,
    @SerializedName("eid")
    val eid: String? = null,
    @SerializedName("fid")
    val fid: String? = null,
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("createDate")
    val createDate: Date? = null
) {
    fun getHashMap() =
        mapOf(
            "notificationType" to notificationType,
            "gid" to gid,
            "eid" to eid,
            "fid" to fid,
            "uid" to uid,
            "message" to message
        )

    fun entityToData(entity: NotificationEntity) = NotificationSendData(
        notificationType = entity.notificationType,
        gid = entity.gid,
        eid = entity.eid,
        fid = entity.fid,
        uid = entity.uid,
        message = entity.message,
        createDate = entity.createData
    )
}

enum class NotificationType(val value: String, val title: String) {
    NOTICE("0", MotorFeedApplication.getContext().getString(R.string.fcm_notice_title)),
    EVENT("1", MotorFeedApplication.getContext().getString(R.string.fcm_event_title)),
    COMMENT("2", MotorFeedApplication.getContext().getString(R.string.fcm_comment_title)),
    RE_COMMENT("3", MotorFeedApplication.getContext().getString(R.string.fcm_re_comment_title));

}