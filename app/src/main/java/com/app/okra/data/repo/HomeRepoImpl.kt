package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ContactResponse
import com.app.okra.models.HomeResponse
import com.app.okra.models.HomeStripeResponse
import com.app.okra.models.SettingRequest
import kotlinx.coroutines.Dispatchers

class HomeRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
    HomeRepo {
    override suspend fun dashboardInfo(time:String): ApiResult<ApiData<HomeResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.dashboardInfo(time)
        }
    }

    override suspend fun stripeInfo(): ApiResult<ApiData<HomeStripeResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.stripeInfo()
        }
    }
}