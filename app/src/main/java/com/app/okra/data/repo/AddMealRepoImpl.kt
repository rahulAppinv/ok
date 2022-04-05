package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.AddMealRequest
import com.app.okra.models.FoodRecognintionResponse
import com.app.okra.models.MealUpdateRequest
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody

class AddMealRepoImpl constructor(
    private val apiService: ApiService,
    private val apiService1: ApiService,
) : BaseRepo(apiService),
    AddMealRepo {

    override suspend fun foodRecognition(
        multipart: MultipartBody.Part?,
        key: String
    ): ApiResult<FoodRecognintionResponse?> {
        return safeApiCallWithoutBaseResponse<FoodRecognintionResponse>(Dispatchers.IO) {
            apiService.foodRecognition(multipart, key)
        }
    }

    override suspend fun addMealLog(params: AddMealRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService1.addMeal(params)
        }
    }
}


