package com.like.drive.motorfeed.ui.feed.upload.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.like.drive.motorfeed.common.livedata.SingleLiveEvent
import com.like.drive.motorfeed.data.feed.FeedData
import com.like.drive.motorfeed.data.motor.MotorTypeData
import com.like.drive.motorfeed.repository.feed.FeedRepository
import com.like.drive.motorfeed.ui.base.BaseViewModel
import com.like.drive.motorfeed.data.photo.PhotoData
import com.like.drive.motorfeed.ui.feed.upload.data.FeedUploadField
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(private val feedRepository: FeedRepository):BaseViewModel(){

    val selectPhotoClickEvent = SingleLiveEvent<Unit>()
    val photoItemClickEvent = SingleLiveEvent<PhotoData>()

    private val _originListData = MutableLiveData<List<PhotoData>>()

    private val _photoListData = MutableLiveData<List<PhotoData>>()
    val photoListData: LiveData<List<PhotoData>> get() = _photoListData

    private val originFileList = ArrayList<PhotoData>()

    private val _pickPhotoCount = MutableLiveData(0)
    val pickPhotoCount :LiveData<Int> get() = _pickPhotoCount

    private val _motorType= MutableLiveData<MotorTypeData>()
    val motorTypeData:LiveData<MotorTypeData> get() = _motorType

    val title = MutableLiveData<String>()
    val content = MutableLiveData<String>()

    val isUploadLoading=SingleLiveEvent<Boolean>()

    private val _isPhotoUpload =MutableLiveData<Boolean>(true)
    val isPhotoUpload : LiveData<Boolean> get() = _isPhotoUpload

    private val _uploadPhotoCount =MutableLiveData(0)
    val uploadPhotoCount :LiveData<Int> get() = _uploadPhotoCount

     val completeEvent = SingleLiveEvent<FeedData>()
     val errorEvent = SingleLiveEvent<Unit>()



    val isFieldEnable = MediatorLiveData<Boolean>().apply {
        addSource(title) {
            value = isResultFieldValue(it, content.value)
        }
        addSource(content) {
            value = isResultFieldValue(title.value, it)
        }
    }


    init {
       // createPhotoList(null)
    }
    /**
     * 초기 리스트 구성
     * 이미 업로드 된 이미지 있으면 표시
     * 없으면 빈 리스트로 초기화
     */
    private fun createPhotoList(uploadedKeys: Array<String>?) {
        if (uploadedKeys?.isNotEmpty() == true) {
            _photoListData.value = uploadedKeys.map {
                PhotoData(imgUrl = it)
            }
        } else {
            _photoListData.value = emptyList()
        }
        _originListData.value = _photoListData.value
    }


    fun addFile(file:File){
        originFileList.add(PhotoData().apply { this.file = file })
        setPhotoSize()
    }

    fun removeFile(photoData: PhotoData){
        originFileList.remove(photoData)
        setPhotoSize()
    }

    /**
     * 사진 클릭 시 이벤트
     */
    fun onClickPhotoItem(photoData: PhotoData) {
        photoItemClickEvent.value = photoData
    }

    fun upload() {

        isUploadLoading.value = true
        _isPhotoUpload.value = originFileList.isNotEmpty()

        val feedField = FeedUploadField(
            title = this.title.value!!,
            content = this.content.value!!,
            motorTypeData = _motorType.value
        )
        viewModelScope.launch {
            feedRepository.addFeed(feedField, originFileList,
                photoSuccessCount = { count ->
                    _uploadPhotoCount.postValue(count)
                    if (count == originFileList.size) {
                        _isPhotoUpload.postValue(false)
                    }
                },
                success = {
                    isUploadLoading.postValue(false)
                    completeEvent.value = it
                },
                fail = {
                    isUploadLoading.value = false
                    errorEvent.call()
                })
        }
    }


    private fun isResultFieldValue(title:String?,content:String?) = !title.isNullOrBlank() && !content.isNullOrBlank()

    private fun setPhotoSize(){ _pickPhotoCount.postValue(originFileList.size) }

    fun isPhotoLimitSize() = originFileList.size < PHOTO_MAX_SIZE

    fun setMotorType(motorTypeData: MotorTypeData){ _motorType.value = motorTypeData}


    companion object{
        val PHOTO_MAX_SIZE = 5
    }

}