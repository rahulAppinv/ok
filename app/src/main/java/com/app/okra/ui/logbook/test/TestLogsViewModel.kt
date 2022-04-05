package com.app.okra.ui.logbook.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.TestLogsRepo
import com.app.okra.models.TestListResponse
import com.app.okra.models.TestUpdateRequest
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.Companion.DATA_LIMIT
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_3
import com.app.okra.utils.Event
import com.app.okra.utils.getDifferentInfoFromDateInString
import com.app.okra.utils.getISOFromDate
import java.util.*

class TestLogsViewModel(private val repo: TestLogsRepo?) : BaseViewModel() {

    private var testListLiveData = MutableLiveData<ApiData<TestListResponse>>()
    val _testListLiveData: LiveData<ApiData<TestListResponse>>
        get() = testListLiveData

    private var updateTestLiveData = MutableLiveData<ApiData<Any>>()
    val _updateTestLiveData: LiveData<ApiData<Any>>
        get() = updateTestLiveData

    private var deleteTestLiveData = MutableLiveData<ApiData<Any>>()
    val _deleteTestLiveData: LiveData<ApiData<Any>>
        get() = deleteTestLiveData

    private var testDetailLiveData = MutableLiveData<ApiData<TestListResponse>>()
    val _testDetailLiveData: LiveData<ApiData<TestListResponse>>
        get() = testDetailLiveData

    var params= WeakHashMap<String, Any>()
    val updateRequest = TestUpdateRequest()



    fun prepareRequest(pageNo: Int,
                       testingTime: String?=null,
                       fromDate: String?=null,
                       toDate: String?=null){
        params.clear()
        params[AppConstants.RequestParam.pageNo] = pageNo
        params[AppConstants.RequestParam.limit] = DATA_LIMIT

        if(!testingTime.isNullOrEmpty()){
            params[AppConstants.RequestParam.testingTime] = testingTime
        }

        if(!fromDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.fromDate] = getDifferentInfoFromDateInString(fromDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, DATE_FORMAT_3)
        }
        if(!toDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.toDate] = getDifferentInfoFromDateInString(toDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, DATE_FORMAT_3)
        }
       /* if(!fromDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.fromDate] = fromDate *//*getISOFromDate(fromDate, DATE_FORMAT_3)*//*
        }
        if(!toDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.toDate] = toDate *//*getISOFromDate(toDate, DATE_FORMAT_3)*//*
        }*/

    }

    fun prepareUpdateRequest(
        testId: String?=null,
        date: String?=null,
        testingTime: String?=null,
        bloodGlucose: Int?=null,
        bloodPressure: Int?=null,
        insulin: Int?=null,
        additionalNotes: String?=null,
        mealsBefore: MutableList<String>?=null,
        mealsAfter: MutableList<String>?=null,
    ){


        updateRequest.testId= testId

        if(!testingTime.isNullOrEmpty()){
            updateRequest.testingTime = testingTime
        }

        bloodGlucose?.let{
            updateRequest.bloodGlucose = it
        }
        bloodPressure?.let{
            updateRequest.bloodPressure = it
        }
        insulin?.let{
            updateRequest.insulin = it
        }
        if(!additionalNotes.isNullOrEmpty()){
            updateRequest.additionalNotes = additionalNotes
        }

        date?.let{
            updateRequest.date = getISOFromDate(it, AppConstants.DateFormat.DATE_FORMAT_5)
        }

        updateRequest.mealsBefore = mealsBefore?.toCollection(ArrayList())
        updateRequest.mealsAfter = mealsAfter?.toCollection(ArrayList())

          }

    fun getTestLogs() {
        launchDataLoad {

            showProgressBar()
            val result = repo?.getTestLogs(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    testListLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

    fun updateTest() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.updateTestLog(updateRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    updateTestLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }
    fun deleteTest(id :String) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.deleteTest(id)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    deleteTestLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

    fun getTestDetails(testId: String) {
        launchDataLoad {

            showProgressBar()
            val result = repo?.getTestDetails(testId)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    testDetailLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

}