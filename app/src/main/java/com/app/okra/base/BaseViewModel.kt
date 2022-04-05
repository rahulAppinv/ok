package com.app.okra.base

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.okra.amazonS3.AmazonS3
import com.app.okra.amazonS3.ImageBean
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.extension.isEmailValid
import com.app.okra.extension.isPasswordValid
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.ProgressDialogData
import com.app.okra.utils.ToastData
import kotlinx.coroutines.*
import java.io.File

abstract class BaseViewModel : ViewModel() {
    protected var progressDialog = MutableLiveData<Event<ProgressDialogData>>()
    val _progressDialog: LiveData<Event<ProgressDialogData>> = progressDialog

    protected var progressDialogForLoadMore = MutableLiveData<Event<ProgressDialogData>>()
    val _progressDialogForLoadMore: LiveData<Event<ProgressDialogData>> = progressDialogForLoadMore


    protected var toastObserver = MutableLiveData<Event<ToastData>>()
    val  _toastObserver: LiveData<Event<ToastData>> = toastObserver


    protected var errorObserver = MutableLiveData<Event<ApiData<Any>>>()
    val  _errorObserver: LiveData<Event<ApiData<Any>>> = errorObserver


    protected var genericErrorObserver = MutableLiveData<Event<ApiResult.GenericError>>()
    val  _genericErrorObserver: LiveData<Event<ApiResult.GenericError>> = genericErrorObserver


    fun onEmailChanged(s: CharSequence, start: Int, before: Int, count: Int) :Boolean {
        if(s.isEmpty() || !s.toString().isEmailValid()){
            return false
        }
        return true
    }

    fun onPasswordChanged(s: CharSequence, start: Int, before: Int, count: Int) :Boolean {
        if (s.isEmpty() ||  !s.toString().isPasswordValid()) {
            return false
        }
        return true
    }
/*
    public fun setLoginInStatus(status: Boolean){
        PreferenceManager.putBoolean(IS_LOGGED_IN,status)
    }

    protected fun getLoginInStatus() :Boolean{
        return PreferenceManager.getBoolean(IS_LOGGED_IN)
    }
*/

    fun launchDataLoad(dispatcher: CoroutineDispatcher = Dispatchers.Main,
                       block : suspend ((scope: CoroutineScope) ->Unit)) : Job {
        return viewModelScope.launch(dispatcher) {
            block.invoke(this)
        }
    }

    // this is general progress bar with fixed message "PROCESSING".
    fun showProgressBar_loadMore(){
        progressDialogForLoadMore.value = Event(ProgressDialogData(true))
    }

    suspend fun hideProgressBar(){
        withContext(Dispatchers.Main) {
            progressDialog.value = Event(ProgressDialogData())
        }
    }

    // this is general progress bar with fixed message "PROCESSING".
    fun showProgressBar(){
        progressDialog.value = Event(ProgressDialogData(true))
    }

    protected fun uploadFile(amazonS3 : AmazonS3? , uri :Uri?) {
        if(uri !=null) {
            progressDialog.value = Event(ProgressDialogData(
                status = true,
                message = MessageConstants.ProgressBar.uploading)
            )
            launchDataLoad {
                amazonS3?.uploadFile(addDataInBean(uri)) }

        }else{
            errorObserver.value = Event(ApiData(message = MessageConstants.Errors.an_error_occurred))
        }
    }


    private fun addDataInBean(uri: Uri): ImageBean {
        val file = File(uri.path!!)
        val bean = ImageBean()
        bean.name = file.name
        bean.imagePath = uri.path!!
        bean.uri = uri
        return bean
    }
}