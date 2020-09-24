package com.like.drive.motorfeed.repository.notice

import com.like.drive.motorfeed.data.notice.NoticeData
import com.like.drive.motorfeed.remote.api.notice.NoticeApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.util.*

class NoticeRepositoryImpl(private val noticeApi: NoticeApi) : NoticeRepository {

    override suspend fun getList(date: Date): Flow<List<NoticeData>> {
        return noticeApi.getNoticeList(date)
    }

    override suspend fun setNotice(
        noticeData: NoticeData,
        success: (NoticeData) -> Unit,
        fail: () -> Unit
    ) {
        noticeApi.setNotice(noticeData).catch { fail.invoke() }.collect {
            success(noticeData)
        }
    }

    override suspend fun removeNotice(
        noticeData: NoticeData,
        success: (NoticeData) -> Unit,
        fail: () -> Unit
    ) {
        noticeApi.removeNotice(noticeData).catch { fail.invoke() }.collect {
            success(noticeData)
        }
    }

}