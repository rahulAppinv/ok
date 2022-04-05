package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.models.OTPVerifyRequest
import com.app.okra.models.ResendOtpRequest
import kotlinx.coroutines.Dispatchers

class OTPVerifyRepoImpl constructor(private val apiService: ApiService, ) : BaseRepo(apiService),
    OTPVerifyRepo {


    override suspend fun executeOTPVerifyRequest(request: OTPVerifyRequest): ApiResult<ApiData<InitialBoardingResponse>> {
       return safeApiCall(Dispatchers.IO){
            apiService.verifyOTP(request)
        }
    }

    override suspend fun executeOTPResendRequest(request: ResendOtpRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO){
            apiService.resendOTP(request)
        }
    }
}