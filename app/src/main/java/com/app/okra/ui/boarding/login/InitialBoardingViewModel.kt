package com.app.okra.ui.boarding.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.InitialBoardingRepo
import com.app.okra.extension.isEmailValid
import com.app.okra.extension.isPasswordValid
import com.app.okra.models.InitialBoardingRequest
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.ToastData

class InitialBoardingViewModel(private val repo : InitialBoardingRepo?) : BaseViewModel() {

    private var loginLiveData = MutableLiveData<ApiData<InitialBoardingResponse>>()
    val _loginLiveData: LiveData<ApiData<InitialBoardingResponse>>
        get() = loginLiveData

    companion object {
        const val FIELD_EMAIL = "email"
        const val FIELD_PASS = "pass"
        const val FIELD_NAME = "name"

    }

    private var deviceToken :String? = null
    init {
         deviceToken  = PreferenceManager.getString(AppConstants.Pref_Key.DEVICE_TOKEN)
    }

    private val initBoardingRequest  = InitialBoardingRequest()
    fun setLoginValue(email: String, password:String){
        initBoardingRequest.email =email
        initBoardingRequest.password =password.trim()
        initBoardingRequest.deviceId = AppConstants.android
        initBoardingRequest.deviceToken =deviceToken
    }

    fun setSignUpValue(
        name: String,
        email: String,
        password:String,
        serialNo:String?=null,
    ){
        initBoardingRequest.name = name
        initBoardingRequest.email =email
        initBoardingRequest.password =password.trim()
        initBoardingRequest.deviceId = AppConstants.android
        initBoardingRequest.deviceToken =deviceToken
        serialNo?.let {
            initBoardingRequest.serialNo =serialNo
        }
    }

    fun login(){
        if(validateData()){
            showProgressBar()

            launchDataLoad {
                val result = repo?.onLogin(initBoardingRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        loginLiveData.value = result.value
                    }
                    is ApiResult.GenericError -> {
                        errorObserver.value = Event(
                            ApiData(null, result.errorCode, result.message,result.type,result.message))
                    }
                    else -> {
                        errorObserver.value = Event(ApiData(message = "Network Issue"))
                    }
                }
            }
        }
    }
    fun signUp( ){
        if(validateSignUpData()){
            launchDataLoad {
                showProgressBar()
                val result = repo?.onSignUp(initBoardingRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success<InitialBoardingResponse> -> {
                        loginLiveData.value = result.value
                    }
                    is ApiResult.GenericError -> {
                        errorObserver.value = Event(ApiData(message =result.message))
                    }
                    is ApiResult.NetworkError -> {
                        errorObserver.value = Event(ApiData(message ="Network Issue"))
                    }
                }
            }
        }
    }

    private fun validateSignUpData(): Boolean {
        return when{
            initBoardingRequest.name.isNullOrBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_name,  FIELD_NAME))
                false
            }
            initBoardingRequest.email.isNullOrBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_email,  FIELD_EMAIL))
                false
            }
            !initBoardingRequest.email!!.isEmailValid() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.invalid_email,  FIELD_EMAIL))
                false
            }
            initBoardingRequest.password.isNullOrBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_pass,  FIELD_PASS))
                false
            }
            !initBoardingRequest.password!!.isPasswordValid() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.invalid_pass_message, FIELD_PASS))
                false
            }

            else -> true
        }
    }

    private fun validateData(): Boolean {
        return when{
            initBoardingRequest.email.isNullOrBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_email,FIELD_EMAIL))
                false
            }
            !initBoardingRequest.email!!.isEmailValid() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.invalid_email, FIELD_EMAIL))
                false
            }
            initBoardingRequest.password.isNullOrBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_pass,FIELD_PASS))
                false
            }
            !initBoardingRequest.password!!.isPasswordValid() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.invalid_pass_message,FIELD_PASS))
                false
            }
            else -> true
        }
    }
}