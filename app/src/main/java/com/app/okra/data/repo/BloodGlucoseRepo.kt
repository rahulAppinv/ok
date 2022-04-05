package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.InsightResponse
import java.util.*

interface BloodGlucoseRepo {
    suspend fun getInsight(params: WeakHashMap<String, Any>): ApiResult<ApiData<InsightResponse>>
}