package com.app.okra.ui.boarding.otpVerify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.OTPVerifyRepo
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.models.OTPVerifyRequest
import com.app.okra.models.ResendOtpRequest
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.ToastData

class OTPVerifyViewModel(private val repo : OTPVerifyRepo?) : BaseViewModel() {

    private var OtpVerifyLiveData = MutableLiveData<ApiData<InitialBoardingResponse>>()
    val _OtpVerifyLiveData: LiveData<ApiData<InitialBoardingResponse>>
        get() = OtpVerifyLiveData

    private var OtpResendLiveData = MutableLiveData<Any>()
    val _OtpResendLiveData: LiveData<Any>
        get() = OtpResendLiveData

    private val otpVerifyRequest  = OTPVerifyRequest()
    private val resendOtpRequest  = ResendOtpRequest()
    private var deviceToken :String? = null

    init {
        deviceToken  = PreferenceManager.getString(AppConstants.Pref_Key.DEVICE_TOKEN)

    }

    fun setResetOTPValue(email: String,screenType: String){
        resendOtpRequest.email =email
        resendOtpRequest.type =screenType
    }

    fun setVerifyOtpValue(email: String, otp :String) {
        otpVerifyRequest.email = email
        otpVerifyRequest.otp = otp
        otpVerifyRequest.deviceId = AppConstants.android
        otpVerifyRequest.deviceToken = PreferenceManager.getString(AppConstants.Pref_Key.DEVICE_TOKEN)
    }

    fun otpVerifyApi(){
        if(validateData()){
            launchDataLoad {
                showProgressBar()
                val result = repo?.executeOTPVerifyRequest(otpVerifyRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> OtpVerifyLiveData.value = result.value
                    is ApiResult.GenericError -> errorObserver.value = Event(ApiData(statusCode = result.errorCode,message = result.message))
                    is ApiResult.NetworkError -> errorObserver.value = Event(ApiData(message = "Network Issue"))
                    else -> errorObserver.value = Event(ApiData(message = MessageConstants.Errors.an_error_occurred))
                }
            }
        }
    }

    fun otpReSendApi(){
        launchDataLoad {
            showProgressBar()
            val result = repo?.executeOTPResendRequest(resendOtpRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> OtpResendLiveData.value = result.value
                is ApiResult.GenericError -> errorObserver.value = Event(ApiData(message = result.message))
                is ApiResult.NetworkError -> errorObserver.value = Event(ApiData(message = "Network Issue"))
                else -> errorObserver.value = Event(ApiData(message = MessageConstants.Errors.an_error_occurred))
            }
        }
    }

    private fun validateData(): Boolean {
        return when{
            otpVerifyRequest.otp.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_otp))
                false
            }
            otpVerifyRequest.otp!!.length != 4 -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_otp))
                false
            }
            else -> true
        }
    }
}