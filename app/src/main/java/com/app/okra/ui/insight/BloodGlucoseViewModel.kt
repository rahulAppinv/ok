package com.app.okra.ui.insight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.BloodGlucoseRepo
import com.app.okra.models.InsightResponse
import com.app.okra.utils.*
import java.util.*

class BloodGlucoseViewModel(private val repo: BloodGlucoseRepo?) : BaseViewModel() {

    private var insightLiveData = MutableLiveData<ApiData<InsightResponse>>()
    val _insightLiveData: LiveData<ApiData<InsightResponse>>
        get() = insightLiveData

    var params= WeakHashMap<String, Any>()

    fun prepareRequest(type: String,
                       testingTime: String,
                       timesOfConsideration: String, ){
        params.clear()
        params[AppConstants.RequestParam.type] = type
        params[AppConstants.RequestParam.timesOfConsideration] = timesOfConsideration
        params[AppConstants.RequestParam.testingTime] = testingTime
    }

    fun getInsight() {
        launchDataLoad {

            showProgressBar()
            val result = repo?.getInsight(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    insightLiveData.value = result.value
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