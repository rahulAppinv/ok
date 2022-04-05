package com.app.okra.ui.connected_devices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.ConnectedDevicesRepo
import com.app.okra.extension.decimalToHexConversion
import com.app.okra.extension.hexToDecimalConversion
import com.app.okra.models.*
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants.Errors.Companion.network_issue
import com.app.okra.utils.getISOFromDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConnectedDevicesViewModel(private val repo: ConnectedDevicesRepo?) : BaseViewModel() {


    private var logoutLiveData = MutableLiveData<ApiData<Any>>()
    val _logoutLiveData: LiveData<ApiData<Any>> get() = logoutLiveData

    private var contactUsLiveData = MutableLiveData<ApiData<ContactResponse>>()
    val _contactUsLiveData: LiveData<ApiData<ContactResponse>> get() = contactUsLiveData

    private var connectedDevicesLiveData = MutableLiveData<ApiData<ArrayList<BLEDeviceListData>>>()
    val _connectedDevicesLiveData: LiveData<ApiData<ArrayList<BLEDeviceListData>>>
        get() = connectedDevicesLiveData

    private var dataCountLiveData = MutableLiveData<ApiData<DeviceDataCount>>()
    val _dataCountLiveData: LiveData<ApiData<DeviceDataCount>>
        get() = dataCountLiveData

    private var testAddLiveData = MutableLiveData<ApiData<Any>>()
    val _testAddLiveData: LiveData<ApiData<Any>>
        get() = testAddLiveData

    var settingRequest= SettingRequest()
    var deviceDataRequest= DeviceDataRequest()
    var testAddRequest= TestAddRequest()


    fun  prepareTestRequest(fetchedTestList : ArrayList<BLETestData>){
        testAddRequest.testData = fetchedTestList
    }

    fun setDeviceDataRequest(deviceName: String?="", deviceKey: String?=""){
        deviceDataRequest.deviceName = deviceName
        deviceDataRequest.deviceUUID = deviceKey
    }

    //  Upload
/*
    fun uploadUserPic() {
        showProgressBar()
        launchDataLoad {
            val result = repo?.uploadUserPic(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    uploadImageLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(result.message)
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event("Network Issue")
                }
            }
        }
    }
*/


    fun getPreviousDevices() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getPreviouslyConnectedDeviceList()
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    connectedDevicesLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = network_issue))
                }
            }
        }
    }

    fun addTestApi() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.addTestData(testAddRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    testAddLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = network_issue))
                }
            }
        }
    }

    fun extractDataFromByteArray(testData: ByteArray?, bleDevice: BleDevice?): BLETestData? {
        var totalCount: String? = ""
        var glucoseCount: String? = ""
        var forData  = false
        val prepareDate = Date()
        val bleTestData = BLETestData(deviceId = bleDevice?.mac, deviceName = bleDevice?.name)

        testData?.let { it ->

            for ((i, data) in it.withIndex()) {
                when (i) {
                    3 -> {
                        if (data.toInt() == 2) {
                            forData = true
                        }
                    } 4 -> {
                    if (data.toInt() != 0 && !forData) {
                        totalCount = decimalToHexConversion(data.toString())
                    }
                }
                    5 -> {

                        if(!forData) {
                            if (data.toInt() != 0) {
                                totalCount?.let {
                                    if (it.isNotEmpty()) {
                                        val localValueByte = decimalToHexConversion(data.toString())
                                        totalCount += localValueByte
                                        totalCount = hexToDecimalConversion(totalCount!!)
                                    } else {
                                        totalCount = data.toString()
                                    }
                                }
                            } else if (!totalCount.isNullOrEmpty()) {
                                totalCount += data.toString()
                                totalCount = hexToDecimalConversion(totalCount!!)
                            }
                            //  println("::::::: 5th Byte: $totalCount")
                            bleTestData.totalDataCount = totalCount
                        }
                    }
                    6 -> { // Command-2 -> for Year
                        if (data.toInt() != 0) {
                            prepareDate.year = data.toString()
                        }
                    }
                    7 -> { // Command-2 -> for month
                        if (data.toInt() != 0) {
                            prepareDate.month = data.toString()
                        }
                    }
                    8 -> { // Command-2 -> for day
                        if (data.toInt() != 0) {
                            prepareDate.day = data.toString()
                        }
                    }
                    9 -> { // Command-2 -> for hour
                        if (data.toInt() != 0) {
                            prepareDate.hour = data.toString()
                        }
                    }
                    10 -> { // Command-2 -> for min
                        if (data.toInt() != 0) {
                            prepareDate.min = data.toString()
                        }
                    }
                    11 -> { // Command-2 -> for sec
                        if (data.toInt() != 0) {
                            prepareDate.sec = data.toString()
                        }
                    }
                    12 -> { // Command-2 -> event value
                        bleTestData.testingTime = getEventValue(data.toInt())
                    }
                    13 -> { // Command-2 -> Glucose data -1
                        if (data.toInt() != 0) {
                            glucoseCount = decimalToHexConversion(data.toString())
                            println("::::::: 14th Byte: $glucoseCount")
                        }
                    }
                    14 -> { // Command-2 -> Glucose data -2
                        if (data.toInt() != 0) {
                            glucoseCount?.let {
                                if (it.isNotEmpty()) {
                                    val localValueByte = decimalToHexConversion(data.toString())
                                    glucoseCount += localValueByte
                                    glucoseCount = hexToDecimalConversion(glucoseCount!!)
                                } else {
                                    glucoseCount = data.toString()
                                }
                            }
                        } else if (!glucoseCount.isNullOrEmpty()) {
                            glucoseCount += data.toString()
                            glucoseCount = hexToDecimalConversion(glucoseCount!!)
                        }
                        //  println("::::::: 5th Byte: $glucoseCount")
                        if(!glucoseCount.isNullOrEmpty())
                            bleTestData.bloodGlucose = glucoseCount!!.toInt()
                    }
                }
            }
        }

        bleTestData.date = getISOFromDate(prepareDate.getCompleteDate(), "dd-MM-yy hh:mm:ss")

        return bleTestData
    }


    fun getTestDataCountFromApi() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getDeviceDataCount(deviceDataRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    dataCountLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = network_issue))
                }
            }
        }
    }

    // Getting locally saved Devices Data List (with count)
    fun getDeviceDataList(): ArrayList<DeviceDataCount> {
        val data =  PreferenceManager.getString(AppConstants.Pref_Key.DEVICES_DATA_COUNT)

        if(!data.isNullOrEmpty()){
            val myType = object : TypeToken<ArrayList<DeviceDataCount>>() {}.type
            return Gson().fromJson(data, myType)
        }
        return ArrayList()
    }

    fun getDeviceDataCount(bleDevice: BleDevice): Int {
        val deviceDataList = getDeviceDataList()
        deviceDataList.let {
            for (singleDevice in it) {
                if (singleDevice.deviceId == bleDevice.mac) {
                    return singleDevice.testCount
                }
            }
        }
        return -1
    }

    // Preparing local list for Device Data and saving it in Preference for local access.
    fun updateDeviceDataList(deviceData: DeviceDataCount) {
        val deviceDataList = getDeviceDataList()
        if (deviceDataList.isNullOrEmpty()) {
            deviceDataList.add(deviceData)
        } else {
            var isDeviceFound = false

            for (singleDevice in deviceDataList) {
                if (singleDevice.deviceId == deviceData.deviceId) {
                    isDeviceFound = true
                    singleDevice.testCount = deviceData.testCount
                    break
                }
            }
            when {
                !isDeviceFound -> {
                    deviceDataList.add(deviceData)
                }
                else -> {}
            }
        }
        PreferenceManager.putString(AppConstants.Pref_Key.DEVICES_DATA_COUNT, Gson().toJson(deviceDataList))

    }

    fun getEventValue(no :Int):String{
        return when(no){
            1->{
                AppConstants.BEFORE_MEAL
            }
            2->{
                AppConstants.AFTER_MEAL
            }
            3->{
                AppConstants.POST_MEDICINE
            }
            4->{
                AppConstants.POST_WORKOUT
            }
            5->{
                AppConstants.CONTROLE_SOLUTION
            }
            else->{
                AppConstants.NO_USE

            }
        }
    }
}