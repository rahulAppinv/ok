package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.BLEDeviceListData
import com.app.okra.models.DeviceDataCount
import com.app.okra.models.DeviceDataRequest
import com.app.okra.models.TestAddRequest


interface ConnectedDevicesRepo {

    suspend fun getPreviouslyConnectedDeviceList()
            : ApiResult<ApiData<ArrayList<BLEDeviceListData>>>

    suspend fun getDeviceDataCount(deviceDataRequest: DeviceDataRequest)
            : ApiResult<ApiData<DeviceDataCount>>
    suspend fun addTestData(addTestRequest: TestAddRequest)
            : ApiResult<ApiData<Any>>

}