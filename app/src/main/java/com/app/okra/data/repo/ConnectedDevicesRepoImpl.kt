package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.BLEDeviceListData
import com.app.okra.models.DeviceDataCount
import com.app.okra.models.DeviceDataRequest
import com.app.okra.models.TestAddRequest
import kotlinx.coroutines.Dispatchers

class ConnectedDevicesRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
    ConnectedDevicesRepo {
    override suspend fun getPreviouslyConnectedDeviceList()
    : ApiResult<ApiData<ArrayList<BLEDeviceListData>>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getPreviouslyConnectedDevices()
        }
    }

    override suspend fun getDeviceDataCount(deviceDataRequest: DeviceDataRequest): ApiResult<ApiData<DeviceDataCount>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getTestDataCount(deviceDataRequest)
        }
    }
    override suspend fun addTestData(addTestRequest: TestAddRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.addTest(addTestRequest)
        }
    }
}