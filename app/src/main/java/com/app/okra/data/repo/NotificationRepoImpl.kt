package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.NotificationRequest
import com.app.okra.models.NotificationResponse
import kotlinx.coroutines.Dispatchers

class NotificationRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
    NotificationRepo {

    override suspend fun getNotification(page:Int, limit:Int): ApiResult<ApiData<NotificationResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.notification(page,limit)
        }
    }

    override suspend fun deleteNotification(body: NotificationRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.deleteNotification(body)
        }
    }
}