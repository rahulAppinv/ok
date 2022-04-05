package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.models.OTPVerifyRequest
import com.app.okra.models.ResendOtpRequest


interface OTPVerifyRepo {
    suspend fun executeOTPVerifyRequest(request : OTPVerifyRequest)
            : ApiResult<ApiData<InitialBoardingResponse>>

    suspend fun executeOTPResendRequest(request : ResendOtpRequest)
            : ApiResult<ApiData<Any>>
}