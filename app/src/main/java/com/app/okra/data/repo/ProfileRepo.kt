package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.UserDetailResponse
import java.util.*

interface ProfileRepo {
    suspend fun apiForProfileInfo(userId: String): ApiResult<ApiData<UserDetailResponse>>
    suspend fun updateProfile(params: WeakHashMap<String, Any>): ApiResult<ApiData<Any>>

}