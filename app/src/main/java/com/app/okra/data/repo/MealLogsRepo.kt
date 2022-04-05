package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.*
import java.util.*

interface MealLogsRepo {
    suspend fun getMealLogs(params: WeakHashMap<String, Any>): ApiResult<ApiData<MealListResponse>>
  //  suspend fun getMealLogs(params: WeakHashMap<String, Any>): ApiResult<ApiData<TestListResponse>>
    suspend fun updateMealLog(params: MealUpdateRequest): ApiResult<ApiData<Any>>
    suspend fun deleteMeal(id: String): ApiResult<ApiData<Any>>
}