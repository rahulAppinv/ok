package com.app.okra.ui.my_account.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.SettingRepo
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.models.ContactResponse
import com.app.okra.models.MealUpdateRequest
import com.app.okra.models.SettingRequest
import com.app.okra.utils.*

class SettingsViewModel(private val repo: SettingRepo?) : BaseViewModel() {

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_PHONE = "phone"
        const val FIELD_AGE = "age"
        const val OTHER = "other"
    }

    private var logoutLiveData = MutableLiveData<ApiData<Any>>()
    val _logoutLiveData: LiveData<ApiData<Any>> get() = logoutLiveData

    private var contactUsLiveData = MutableLiveData<ApiData<ContactResponse>>()
    val _contactUsLiveData: LiveData<ApiData<ContactResponse>> get() = contactUsLiveData

    private var settingLiveData = MutableLiveData<ApiData<Any>>()
    val _settingLiveData: LiveData<ApiData<Any>>
        get() = settingLiveData

    private var deleteAccountData = MutableLiveData<ApiData<Any>>()
    val _deleteAccountData : LiveData<ApiData<Any>>
        get() = deleteAccountData

    var settingRequest= SettingRequest()



    fun  setSettingRequest(
            inAppStatus: Boolean?=null,
            pushNotification: Boolean?=null,
            bloodGlucoseUnit: String?=null,
            hyperBloodGlucoseValue: String?=null,
            hypoBloodGlucoseValue: String?=null
    ){
        settingRequest = SettingRequest()

        inAppStatus?.let {
            settingRequest.inappNotificationStatus = it
        }
        pushNotification?.let {
            settingRequest.pushNotificationStatus = it
        }
        bloodGlucoseUnit?.let {
            settingRequest.bloodGlucoseUnit = it
        }
        hyperBloodGlucoseValue?.let {
            settingRequest.hyperBloodGlucoseValue = it
        }
        hypoBloodGlucoseValue?.let {
            settingRequest.hypoBloodGlucoseValue = it
        }
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


    fun updateSettings() {
        launchDataLoad {
            if(validateRequest()) {
                showProgressBar()
                val result = repo?.updateNotificationStatus(settingRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        settingLiveData.value = result.value
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


    private fun validateRequest(): Boolean {
        return when{
            settingRequest.hyperBloodGlucoseValue.isNullOrBlank() ->{
                toastObserver.value = Event(ToastData(MessageConstants.Errors.please_fill_hyper_values))
                false
            }
            settingRequest.hypoBloodGlucoseValue.isNullOrBlank() ->{
                toastObserver.value = Event(ToastData(MessageConstants.Errors.please_fill_hyper_values))
                false
            }
            settingRequest.hypoBloodGlucoseValue!!.toFloat() > settingRequest.hyperBloodGlucoseValue!!.toFloat()->{
                toastObserver.value = Event(ToastData(MessageConstants.Errors.hypo_cant_be_greater))
                false
            }
            else -> true
        }
    }


    fun onLogout() {
        launchDataLoad {
            showProgressBar()
            val result = (repo as SettingRepoImpl).onLogout()
            hideProgressBar()
            when (result) {
                is ApiResult.Success<Any> ->{
                    logoutLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }


    fun getContactUsApi() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.contactUs()
            hideProgressBar()
            when (result) {
                is ApiResult.Success ->{
                    contactUsLiveData.value = result.value
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
    fun executeDeleteApi(email: String) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.deleteAccountApi(email)
            hideProgressBar()
            when (result) {
                is ApiResult.Success ->{
                    deleteAccountData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

}