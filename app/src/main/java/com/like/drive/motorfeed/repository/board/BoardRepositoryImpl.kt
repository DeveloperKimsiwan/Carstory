package com.like.drive.motorfeed.repository.board

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctionsException
import com.like.drive.motorfeed.cache.dao.like.LikeDao
import com.like.drive.motorfeed.cache.entity.LikeEntity
import com.like.drive.motorfeed.common.user.UserInfo
import com.like.drive.motorfeed.data.board.CommentData
import com.like.drive.motorfeed.data.board.CommentWrapData
import com.like.drive.motorfeed.data.board.BoardData
import com.like.drive.motorfeed.data.board.ReCommentData
import com.like.drive.motorfeed.data.motor.MotorTypeData
import com.like.drive.motorfeed.data.notification.NotificationSendData
import com.like.drive.motorfeed.data.notification.NotificationType
import com.like.drive.motorfeed.data.photo.PhotoData
import com.like.drive.motorfeed.remote.api.board.BoardApi
import com.like.drive.motorfeed.remote.api.img.ImageApi
import com.like.drive.motorfeed.remote.api.notification.NotificationApi
import com.like.drive.motorfeed.remote.reference.CollectionName
import com.like.drive.motorfeed.ui.board.data.LikeCountEnum
import com.like.drive.motorfeed.ui.board.category.data.CategoryData
import com.like.drive.motorfeed.ui.board.upload.data.BoardUploadField
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class BoardRepositoryImpl(
    private val boardApi: BoardApi,
    private val imgApi: ImageApi,
    private val firestore: FirebaseFirestore,
    private val notificationApi: NotificationApi,
    private val likeDao: LikeDao
) : BoardRepository {

    private var photoFileList = ArrayList<PhotoData>()
    private lateinit var documentID: String

    override suspend fun addBoard(
        boardField: BoardUploadField,
        photoFileList: ArrayList<PhotoData>,
        photoSuccessCount: (Int) -> Unit,
        success: (BoardData) -> Unit,
        fail: () -> Unit
    ) {

        this.photoFileList = photoFileList
        documentID = firestore.collection(CollectionName.BOARD).document().id

        if (photoFileList.isNotEmpty()) {
            checkImgUpload().collect {
                photoSuccessCount(it)
            }
        }

        val creteBoardData = BoardData().createData(
            bid = documentID,
            boardUploadField = boardField,
            motorTypeData = boardField.motorTypeData,
            imgList = photoFileList,
            boardTagList = boardField.tagList
        )

        boardApi.setBoard(creteBoardData)
            .zip(boardApi.setUserBoard(creteBoardData.userInfo?.uid ?: "", creteBoardData)) { t1, t2 ->
                if (t1 && t2) {
                    success(creteBoardData)
                } else {
                    fail.invoke()
                }
            }
            .catch {
                fail.invoke()
            }
            .collect()
    }

    override suspend fun updateBoard(
        boardField: BoardUploadField,
        boardData: BoardData,
        success: (BoardData) -> Unit,
        fail: () -> Unit
    ) {
        val updateFeedData = BoardData().updateData(
            boardUploadField = boardField,
            motorTypeData = boardField.motorTypeData,
            boardData = boardData,
            boardTagList = boardField.tagList
        )

        boardApi.setBoard(updateFeedData)
            .zip(
                boardApi.setUserBoard(
                    updateFeedData.userInfo?.uid ?: "",
                    updateFeedData
                )
            ) { t1, t2 ->
                if (t1 && t2) {
                    success(updateFeedData)
                } else {
                    fail.invoke()
                }
            }
            .catch {
                fail.invoke()
            }
            .collect()
    }

    override suspend fun removeBoard(
        boardData: BoardData,
        success: (BoardData) -> Unit,
        fail: () -> Unit
    ) {

        if (boardData.imageUrls?.isNotEmpty() == true) {
            boardData.imageUrls.forEachIndexed { index, _ ->
                imgApi.deleteBoardImage(boardData.bid ?: "", index).catch { fail.invoke() }.collect()
            }
        }

        boardApi.removeBoard(boardData).zip(boardApi.removeUserBoard(boardData)) { t1, t2 ->
            if (t1 && t2) {
                success(boardData)
            } else {
                fail.invoke()
            }
        }.catch {
            fail.invoke()
        }.collect()

    }

    override suspend fun removeEmptyBoard(
        boardData: BoardData
    ) {
        boardApi.removeUserBoard(boardData).catch { Unit }.collect()
    }

    override suspend fun setLike(bid: String, isUp: Boolean) {
        if (isUp) {
            boardApi.updateCount(bid, LikeCountEnum.LIKE)
            likeDao.insertBid(LikeEntity(bid = bid))
        } else {
            likeDao.deleteBid(bid)
            boardApi.updateCount(bid, LikeCountEnum.UNLIKE)
        }
    }

    override suspend fun getBoardComment(bid: String): Flow<List<CommentData>> {
        return boardApi.getComment(bid)
    }

    override suspend fun getBoard(
        bid: String,
        success: (BoardData?, List<CommentWrapData>?) -> Unit,
        isLike: (Boolean) -> Unit,
        fail: () -> Unit
    ) {
        boardApi.getBoard(bid)
            .zip(boardApi.getComment(bid).zip(boardApi.getReComment(bid)) { comment, reComment ->

                val list = mutableListOf<CommentWrapData>()

                comment.forEach {
                    val reCommentList =
                        reComment.filter { reCommentData -> reCommentData.cid == it.cid }
                            .toMutableList()

                    reCommentList.sortBy { item -> item.createDate }

                    list.add(CommentWrapData(commentData = it, reCommentList = reCommentList))
                }

                list.sortBy { it.commentData.createDate }

                return@zip list
            }) { feedData, commentWrapList ->

                feedData?.bid?.let {
                    isLike(isLikeBoard(bid))
                }

                success.invoke(feedData, commentWrapList)
            }.catch {
                fail.invoke()
            }.collect()
    }

    override suspend fun getBoardList(
        date: Date,
        motorTypeData: MotorTypeData?,
        feedTypeData: CategoryData?,
        tagStr: String?
    ): Flow<List<BoardData>> =
        boardApi.getBoardList(date, motorTypeData, feedTypeData, tagStr)

    override suspend fun getUserBoardList(date: Date, uid: String): Flow<List<BoardData>> =
        boardApi.getUserBoardList(date, uid)

    override suspend fun addComment(
        boardData: BoardData,
        comment: String,
        success: (CommentData) -> Unit,
        fail: () -> Unit
    ) {
        val commentData =
            CommentData().createComment(bid = boardData.bid ?: "", commentStr = comment)

        boardApi.addComment(commentData).catch { e ->
            e.printStackTrace()
            fail.invoke()
        }.collect {
            success(commentData)

            if (boardData.userInfo?.uid != UserInfo.userInfo?.uid) {
                val data = NotificationSendData(
                    notificationType = NotificationType.COMMENT.value,
                    uid = boardData.userInfo?.uid,
                    bid = boardData.bid,
                    title = boardData.title,
                    message = comment
                )
                notificationApi.sendNotification(data).catch { e ->
                    if (e is FirebaseFunctionsException) {
                        e.message?.let { Timber.i(it) }
                    }
                }.collect()
            }
        }
    }

    override suspend fun addReComment(
        boardData: BoardData,
        commentData: CommentData,
        reComment: String,
        success: (ReCommentData) -> Unit,
        fail: () -> Unit
    ) {
        val reCommentData =
            ReCommentData().createComment(
                bid = boardData.bid ?: "",
                cid = commentData.cid ?: "",
                commentStr = reComment
            )

        boardApi.addReComment(reCommentData).catch {
            fail.invoke()
        }.collect {
            success(reCommentData)

            if (commentData.userInfo?.uid != UserInfo.userInfo?.uid) {
                val data = NotificationSendData(
                    notificationType = NotificationType.RE_COMMENT.value,
                    uid = commentData.userInfo?.uid,
                    bid = boardData.bid,
                    title = commentData.commentStr,
                    message = reComment
                )
                notificationApi.sendNotification(data).catch { e ->
                    if (e is FirebaseFunctionsException) {
                        e.message?.let { Timber.i(it) }
                    }
                }.collect()
            }
        }
    }

    override suspend fun updateComment(
        comment: CommentData,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        boardApi.updateComment(comment).catch {
            fail()
        }.collect {
            success()
        }
    }

    override suspend fun updateReComment(
        reCommentData: ReCommentData,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        boardApi.updateReComment(reCommentData).catch {
            fail()
        }.collect {
            success()
        }
    }

    override suspend fun removeComment(
        comment: CommentData,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        boardApi.removeComment(comment).catch {
            fail()
        }.collect {
            success()
        }
    }

    override suspend fun removeReComment(
        reCommentData: ReCommentData,
        success: () -> Unit,
        fail: () -> Unit
    ) {
        boardApi.removeReComment(reCommentData).catch {
            fail()
        }.collect {
            success()
        }
    }

    override suspend fun isLikeBoard(bid: String): Boolean {
        return likeDao.isBoardLike(bid).isNotEmpty()
    }

    override suspend fun removeAllLike() {
        likeDao.deleteLike()
    }

    private suspend fun checkImgUpload(): Flow<Int> =
        flow {
            if (photoFileList.any { it.imgUrl == null }) {
                imgUpload().collect {
                    emit(it)
                }
            }
            emit(photoFileList.size)
        }

    private suspend fun imgUpload(): Flow<Int> = flow {
        photoFileList.forEachIndexed { index, photoData ->
            if (photoData.imgUrl == null) {
                imgApi.uploadBoardImage(documentID, photoData.file!!, index)
                    .catch { photoFileList[index].imgUrl = null }
                    .collect {
                        photoFileList[index].imgUrl = it.toString()
                        emit(photoFileList.filter { photoData -> photoData.imgUrl != null }.size)
                        checkImgUpload()
                    }
            }
        }
    }

}