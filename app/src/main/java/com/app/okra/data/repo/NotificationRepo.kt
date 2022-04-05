package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.InsightResponse
import com.app.okra.models.NotificationRequest
import com.app.okra.models.NotificationResponse
import com.app.okra.models.TestListResponse
import java.util.*

interface NotificationRepo {
    suspend fun getNotification(page:Int, limit:Int): ApiResult<ApiData<NotificationResponse>>
    suspend fun deleteNotification(body: NotificationRequest): ApiResult<ApiData<Any>>
}