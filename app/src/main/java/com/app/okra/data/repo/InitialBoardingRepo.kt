package com.app.okra.data.repo

import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiData
import com.app.okra.models.InitialBoardingRequest
import com.app.okra.models.InitialBoardingResponse


interface InitialBoardingRepo {

    suspend fun onLogin(request : InitialBoardingRequest)
            : ApiResult<ApiData<InitialBoardingResponse>>

    suspend fun onSignUp(request : InitialBoardingRequest)
            : ApiResult<ApiData<InitialBoardingResponse>>

}