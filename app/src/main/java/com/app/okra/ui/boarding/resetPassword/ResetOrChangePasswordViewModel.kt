package com.app.okra.ui.boarding.resetPassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.ResetPasswordRepo
import com.app.okra.extension.isPasswordValid
import com.app.okra.models.ResetPasswordRequest
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.MessageConstants.Errors.Companion.network_issue
import com.app.okra.utils.ToastData

class ResetOrChangePasswordViewModel(private val repo : ResetPasswordRepo?) : BaseViewModel() {

    companion object {
        const val FIELD_1 = "field_1"
        const val FIELD_2 = "field_2"
        const val OTHER = "other"
    }

    private var resetPassLiveData = MutableLiveData<Any>()
    val _resetPassLiveData: LiveData<Any>
        get() = resetPassLiveData

    private var resetPassRequest  = ResetPasswordRequest()


    fun setValue(email: String, newPass :String, confirmPass: String){
        resetPassRequest.email = email
        resetPassRequest.newPassword = newPass.trim()
        resetPassRequest.confirmPassword =confirmPass.trim()
    }


    fun setValue_changePassword(oldPass :String, pass: String){
        resetPassRequest= ResetPasswordRequest()
        resetPassRequest.oldPassword = oldPass.trim()
        resetPassRequest.password =pass.trim()
    }

    fun resetPassApi(){
        if(validateData()){
            launchDataLoad {
                showProgressBar()
                val result = repo?.executeResetPasswordRequest(resetPassRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> resetPassLiveData.value = result.value
                    is ApiResult.GenericError -> errorObserver.value = Event(ApiData(message = result.message))
                    is ApiResult.NetworkError -> errorObserver.value = Event(ApiData(message = network_issue))
                    else -> errorObserver.value = Event(ApiData(message =MessageConstants.Errors.an_error_occurred))
                }
            }
        }
    }
    fun changePassApi(){
        if(validateChangePassData()){
            launchDataLoad {
                showProgressBar()
                val result = repo?.changePasswordRequest(resetPassRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> resetPassLiveData.value = result.value
                    is ApiResult.GenericError -> errorObserver.value = Event(ApiData(message = result.message))
                    is ApiResult.NetworkError -> errorObserver.value = Event(ApiData(message = network_issue))
                    else -> errorObserver.value = Event(ApiData(message =MessageConstants.Errors.an_error_occurred))
                }
            }
        }
    }

       private fun validateData(): Boolean {
        return when{
            resetPassRequest.newPassword.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_pass, FIELD_1 ))
                false
            }
            !resetPassRequest.newPassword!!.isPasswordValid() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_pass, FIELD_1))
                false
            }
            resetPassRequest.confirmPassword.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_confirm_pass, FIELD_2))
                false
            }
            !resetPassRequest.confirmPassword!!.isPasswordValid() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_confirm_pass, FIELD_2))
                false
            }
            !resetPassRequest.newPassword.equals(resetPassRequest.confirmPassword)-> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.password_mismatch, OTHER))
                false
            }
            else -> true
        }
    }
       private fun validateChangePassData(): Boolean {
        return when{
            resetPassRequest.oldPassword.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_current_password,FIELD_1))
                false
            }
            !resetPassRequest.oldPassword.equals(PreferenceManager.getString(AppConstants.Pref_Key.PASSWORD)) -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_current_pass, FIELD_1))
                false
            }
            resetPassRequest.password.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_pass, FIELD_2))
                false
            }
            !resetPassRequest.password!!.isPasswordValid() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_pass, FIELD_2))
                false
            }
            resetPassRequest.oldPassword.equals(resetPassRequest.password)-> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.same_password_issue, OTHER))
                false
            }
            else -> true
        }
    }
}