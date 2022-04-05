package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ReminderRequest
import com.app.okra.models.UserDetailResponse
import kotlinx.coroutines.Dispatchers
import java.util.HashMap

class ReminderRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
    ReminderRepo {
    override suspend fun setReminder(data: HashMap<String, Any>): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.setReminder(data)
        }
    }

    override suspend fun apiForProfileInfo(userId: String): ApiResult<ApiData<UserDetailResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getUserProfile(userId)
        }
    }
}