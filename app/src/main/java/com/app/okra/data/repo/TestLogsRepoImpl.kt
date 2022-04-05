package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ContactResponse
import com.app.okra.models.TestListResponse
import com.app.okra.models.TestUpdateRequest
import com.app.okra.models.UserDetailResponse
import kotlinx.coroutines.Dispatchers
import retrofit2.http.QueryMap
import java.util.*

class TestLogsRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
    TestLogsRepo {

    override suspend fun getTestLogs(params: WeakHashMap<String, Any>): ApiResult<ApiData<TestListResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getTestLogs(params)
        }
    }

    override suspend fun getTestDetails(testId: String): ApiResult<ApiData<TestListResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getTestDetails(testId)
        }
    }

    override suspend fun updateTestLog(params: TestUpdateRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.updateTest(params)
        }
    }

    override suspend fun deleteTest(id: String): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.deleteTest(id)
        }
    }

}


