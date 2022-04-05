package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.UserDetailResponse
import kotlinx.coroutines.Dispatchers
import java.util.*

class ProfileRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
        ProfileRepo {

    override suspend fun apiForProfileInfo(userId: String): ApiResult<ApiData<UserDetailResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getUserProfile(userId)
        }
    }

    override suspend fun updateProfile(params: WeakHashMap<String, Any>): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.updateProfile(params)
        }
    }

}


