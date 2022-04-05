package com.app.okra.ui.boarding.forgotPassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.ForgotPasswordRepo
import com.app.okra.extension.isEmailValid
import com.app.okra.models.ForgotPasswordRequest
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.ToastData

class ForgotPasswordViewModel(private val repo : ForgotPasswordRepo?) : BaseViewModel() {

    private var fPasswordLiveData = MutableLiveData<Any>()
    val _fPasswordLiveData: LiveData<Any>
        get() = fPasswordLiveData


    private val forgotPassRequest  = ForgotPasswordRequest()

    fun setValue(email: String){
        forgotPassRequest.email =email
    }

    fun forgotPassApi(){
        if(validateData()){
            launchDataLoad {
                showProgressBar()
                val result = repo?.executeForgotPasswordRequest(forgotPassRequest)
                hideProgressBar()

                when (result) {
                    is ApiResult.Success -> {
                        fPasswordLiveData.value = result.value
                    }
                    is ApiResult.GenericError -> {
                        errorObserver.value = Event(ApiData(message = result.message))
                    }
                    is ApiResult.NetworkError -> {
                        errorObserver.value = Event(ApiData(message = MessageConstants.Errors.network_issue))
                    }
                    else -> {
                        errorObserver.value = Event(ApiData(message = MessageConstants.Errors.an_error_occurred))
                    }
                }
            }
        }
    }

    private fun validateData(): Boolean {
        return when{
            forgotPassRequest.email.isNullOrBlank() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.enter_email))
                false
            }
            !forgotPassRequest.email!!.isEmailValid() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_email))
                false
            }
            else -> true
        }
    }
}