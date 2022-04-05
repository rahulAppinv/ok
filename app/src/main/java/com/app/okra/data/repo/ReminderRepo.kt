package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.UserDetailResponse

interface ReminderRepo {

    suspend fun setReminder(data : HashMap<String,Any>)
            : ApiResult<ApiData<Any>>
    suspend fun apiForProfileInfo(userId: String): ApiResult<ApiData<UserDetailResponse>>

}