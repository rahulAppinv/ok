package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult


interface DashboardRepo {
    suspend fun onLogout(): ApiResult<ApiData<Any>>
}