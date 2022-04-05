package com.app.okra.data.repo

import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.InitialBoardingRequest
import com.app.okra.models.InitialBoardingResponse
import kotlinx.coroutines.Dispatchers

class InitialBoardingRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
    InitialBoardingRepo {

        override suspend fun onLogin(request: InitialBoardingRequest) :
            ApiResult<ApiData<InitialBoardingResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.login(request)
        }
    }

    override suspend fun onSignUp(request: InitialBoardingRequest): ApiResult<ApiData<InitialBoardingResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.signUp(request)
        }
    }


}