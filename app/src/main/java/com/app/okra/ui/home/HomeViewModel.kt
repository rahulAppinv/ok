package com.app.okra.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.HomeRepo
import com.app.okra.data.repo.SettingRepo
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.models.ContactResponse
import com.app.okra.models.HomeResponse
import com.app.okra.models.HomeStripeResponse
import com.app.okra.models.SettingRequest
import com.app.okra.utils.*

class HomeViewModel(private val repo: HomeRepo?) : BaseViewModel() {

    private var stripeInfoLiveData = MutableLiveData<ApiData<HomeStripeResponse>>()
    val _stripeInfoLiveData: LiveData<ApiData<HomeStripeResponse>> get() = stripeInfoLiveData

    private var dashboardLiveData = MutableLiveData<ApiData<HomeResponse>>()
    val _dashboardLiveData: LiveData<ApiData<HomeResponse>>
        get() = dashboardLiveData

    fun dashboardInfo(time:String) {
        launchDataLoad {
             showProgressBar()
                val result = repo?.dashboardInfo(time)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        dashboardLiveData.value = result.value
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

    fun stripeInfo() {
        launchDataLoad {
            val result = repo?.stripeInfo()
            hideProgressBar()
            when (result) {
                is ApiResult.Success ->{
                    stripeInfoLiveData.value = result.value
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