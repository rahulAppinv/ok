package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.ReportRequest
import com.app.okra.models.UserDetailResponse
import retrofit2.http.QueryMap
import java.util.*

interface ReportRepo {
    suspend fun getReportUrl(params: WeakHashMap<String, Any>): ApiResult<ApiData<String>>

}