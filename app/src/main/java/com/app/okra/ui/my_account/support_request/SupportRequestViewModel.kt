package com.app.okra.ui.my_account.support_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.SupportRequestRepo
import com.app.okra.extension.isPasswordValid
import com.app.okra.models.ContactResponse
import com.app.okra.models.SupportResponse
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordViewModel
import com.app.okra.utils.*
import java.util.*

class SupportRequestViewModel(private val repo: SupportRequestRepo?) : BaseViewModel() {

    companion object {
        const val TITLE ="TITLE"
        const val DESCRIPTION ="DESCRIPTION"
    }

    private var supportListLiveData = MutableLiveData<ApiData<List<SupportResponse>>>()
    val _supportListLiveData: LiveData<ApiData<List<SupportResponse>>> get() = supportListLiveData

    private var updateListLiveData = MutableLiveData<Event<Any>>()
    val _updateListLiveData : LiveData<Event<Any>> get() = updateListLiveData


    private var sendSupportLiveData = MutableLiveData<Event<ApiData<Any>>>()
    val _sendSupportLiveData: LiveData<Event<ApiData<Any>>> get() = sendSupportLiveData

    private lateinit var params: WeakHashMap<String, Any>

    fun updateListAfterAdd(){
        updateListLiveData.value = Event("New value added")
    }

    fun  setRequest(
        pageNo: String?=null,
        limit: String?=null,
        title: String?=null,
        description: String?=null,
    ){

        params = WeakHashMap<String, Any>()

        pageNo?.let{
            params[AppConstants.RequestParam.pageNo] =pageNo
        }

        limit?.let{
            params[AppConstants.RequestParam.limit] =it
        }
        title?.let{
            params[AppConstants.RequestParam.title] =it
        }
        description?.let{
            params[AppConstants.RequestParam.description] =it
        }
    }



    fun getSupportListApi() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getSupportRequestList(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success ->{
                    supportListLiveData.value = result.value
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

    fun sendSupportRequestApi() {
        if(validateData()){

            launchDataLoad {
                showProgressBar()
                val result = repo?.sendSupportRequest(params)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        sendSupportLiveData.value = Event(result.value)
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

    private fun validateData(): Boolean {
        return when{
            !params.containsKey(AppConstants.RequestParam.title)
                    || params[AppConstants.RequestParam.title].toString().isEmpty() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.please_enter_title,
                    TITLE
                ))
                false
            }
            !params.containsKey(AppConstants.RequestParam.description)
                    || params[AppConstants.RequestParam.description].toString().isEmpty() -> {
                toastObserver.value = Event(ToastData(MessageConstants.Errors.please_enter_description,
                    DESCRIPTION
                ))
                false
            }
            else -> true
        }
    }


}