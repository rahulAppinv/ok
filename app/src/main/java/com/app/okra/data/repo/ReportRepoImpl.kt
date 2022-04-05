package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import kotlinx.coroutines.Dispatchers
import java.util.*

class ReportRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
        ReportRepo {


    override suspend fun getReportUrl(params: WeakHashMap<String, Any>): ApiResult<ApiData<String>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getReport(params)
        }
    }
}


