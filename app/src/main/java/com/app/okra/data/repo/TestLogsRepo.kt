package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.TestListResponse
import com.app.okra.models.TestUpdateRequest
import kotlinx.coroutines.Dispatchers
import java.util.*

interface TestLogsRepo {
    suspend fun getTestLogs(params: WeakHashMap<String, Any>): ApiResult<ApiData<TestListResponse>>
    suspend fun getTestDetails(testId: String): ApiResult<ApiData<TestListResponse>>
    suspend fun updateTestLog(params: TestUpdateRequest): ApiResult<ApiData<Any>>
    suspend fun deleteTest(id: String): ApiResult<ApiData<Any>>

}