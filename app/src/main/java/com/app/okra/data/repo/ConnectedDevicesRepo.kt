package com.app.okra.data.repo

import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiData
import com.app.okra.models.*


interface ConnectedDevicesRepo {

    suspend fun getPreviouslyConnectedDeviceList()
            : ApiResult<ApiData<ArrayList<BLEDeviceListData>>>

    suspend fun getDeviceDataCount(deviceDataRequest: DeviceDataRequest)
            : ApiResult<ApiData<DeviceDataCount>>
    suspend fun addTestData(addTestRequest: TestAddRequest)
            : ApiResult<ApiData<Any>>

}