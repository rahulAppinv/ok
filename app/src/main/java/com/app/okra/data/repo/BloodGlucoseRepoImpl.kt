package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.InsightResponse
import com.app.okra.models.TestListResponse
import kotlinx.coroutines.Dispatchers
import java.util.*

class BloodGlucoseRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
    BloodGlucoseRepo {

    override suspend fun getInsight(params: WeakHashMap<String, Any>): ApiResult<ApiData<InsightResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getInsight(params)
        }
    }
}