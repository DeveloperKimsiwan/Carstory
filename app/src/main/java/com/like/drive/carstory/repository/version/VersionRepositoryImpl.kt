package com.like.drive.carstory.repository.version


import com.like.drive.carstory.common.user.UserInfo
import com.like.drive.carstory.data.common.Version
import com.like.drive.carstory.remote.api.version.VersionApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class VersionRepositoryImpl(private val versionApi: VersionApi) : VersionRepository {

    override suspend fun checkMotorTypeVersion(
        insertMotorType: () -> Unit,
        passInsertMotorType: () -> Unit,
        fail: () -> Unit
    ) {
        versionApi.getMotorTypeVersion().
        catch {
            e->e.printStackTrace()
            fail.invoke()
        }.
        collect { value: Version? ->
            value?.version?.let {
                if (UserInfo.userPref.motorTypeVersion ?: 0 < it) {
                    UserInfo.userPref.motorTypeVersion = it
                    insertMotorType.invoke()
                } else {
                    passInsertMotorType.invoke()
                }
            }?: fail.invoke()
        }
    }
}