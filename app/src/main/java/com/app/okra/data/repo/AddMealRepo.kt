package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.AddMealRequest
import com.app.okra.models.FoodRecognintionResponse
import okhttp3.MultipartBody

interface AddMealRepo {
    suspend fun foodRecognition(multipart: MultipartBody.Part?,
                                key: String): ApiResult<FoodRecognintionResponse?>
    suspend fun addMealLog(params: AddMealRequest): ApiResult<ApiData<Any>>
}