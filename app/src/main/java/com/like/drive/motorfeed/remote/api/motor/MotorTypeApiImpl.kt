package com.like.drive.motorfeed.remote.api.motor

import com.google.firebase.firestore.FirebaseFirestore
import com.like.drive.motorfeed.common.ResultState
import com.like.drive.motorfeed.data.motor.MotorTypeData
import com.like.drive.motorfeed.data.common.Version
import com.like.drive.motorfeed.remote.common.FireBaseTask
import com.like.drive.motorfeed.remote.reference.CollectionName
import com.like.drive.motorfeed.remote.reference.DocumentName

class MotorTypeApiImpl(private val fireBaseTask: FireBaseTask,
                       private val fireStore: FirebaseFirestore) :MotorTypeApi{

    override suspend fun getMotorTypeList(): ResultState<List<MotorTypeData>> {
        return fireBaseTask.getData(fireStore.collection(CollectionName.MOTOR_TYPE_LIST),
            MotorTypeData::class.java)
    }

}