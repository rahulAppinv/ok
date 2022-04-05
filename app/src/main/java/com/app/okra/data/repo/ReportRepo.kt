package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import java.util.*

interface ReportRepo {
    suspend fun getReportUrl(params: WeakHashMap<String, Any>): ApiResult<ApiData<String>>

}