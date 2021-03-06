package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.HomeResponse
import com.app.okra.models.HomeStripeResponse

interface HomeRepo {

    suspend fun dashboardInfo(time:String)
            : ApiResult<ApiData<HomeResponse>>
    suspend fun stripeInfo()
            : ApiResult<ApiData<HomeStripeResponse>>

}