package com.app.okra.ui.profile.profile_details

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.amazonS3.AmazonS3
import com.app.okra.amazonS3.ImageBean
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.ProfileRepo
import com.app.okra.extension.isPhoneNumberValid
import com.app.okra.models.UserDetailResponse
import com.app.okra.ui.boarding.login.InitialBoardingViewModel
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.RequestParam.Companion.age
import com.app.okra.utils.AppConstants.RequestParam.Companion.mobileNo
import com.app.okra.utils.AppConstants.RequestParam.Companion.name
import com.app.sensor.amazonS3.AmazonS3Callbacks
import java.util.*

class ProfileViewModel(private val repo: ProfileRepo?) : BaseViewModel(), AmazonS3Callbacks {

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_PHONE = "phone"
        const val FIELD_AGE = "age"
        const val OTHER = "other"
    }

    private var logoutLiveData = MutableLiveData<ApiData<Any>>()
    val _logoutLiveData: LiveData<ApiData<Any>> get() = logoutLiveData

    private var amazonStatusLiveData = MutableLiveData<ImageBean>()
    val _amazonStatusLiveData: LiveData<ImageBean>
        get() = amazonStatusLiveData



    private var profileInfoLiveData = MutableLiveData<ApiData<UserDetailResponse>>()
    val _profileInfoLiveData: LiveData<ApiData<UserDetailResponse>>
        get() = profileInfoLiveData

    private var updateProfileLiveData = MutableLiveData<ApiData<Any>>()
    val _updateProfileLiveData: LiveData<ApiData<Any>>
        get() = updateProfileLiveData

    var params= WeakHashMap<String, Any>()

    private val mAmazonS3: AmazonS3 = AmazonS3()


    fun uploadFile(uri: Uri?) {
        uploadFile(mAmazonS3, uri)
    }

    fun setAmazonCallback(activity : Activity){
        mAmazonS3.setCallback(activity, this)
    }

    override fun uploadSuccess(imageBean: ImageBean) {
        amazonStatusLiveData.value = imageBean
        progressDialog.value = Event(ProgressDialogData(status = false))
    }

    override fun uploadError(e: Exception, imageBean: ImageBean) {
        progressDialog.value = Event(ProgressDialogData(status = false))
        errorObserver.value = Event(ApiData(message = e.message!!))
    }

    override fun uploadProgress(imageBean: ImageBean) {}

    override fun uploadFailed(imageBean: ImageBean) {
        progressDialog.value = Event(ProgressDialogData(status = false))
        errorObserver.value = Event(ApiData(message =MessageConstants.Errors.upload_failed))
    }


    fun  setProfileRequest(
            name: String?=null,
            age: String?=null,
            phoneNo: String?=null,
            profilePic: String?=null
    ){
        params = WeakHashMap<String, Any>()

        name?.let {
            if(it.isNotEmpty())
                params[AppConstants.RequestParam.name] = name
        }

        age?.let {
            if(it.isNotEmpty())
                params[AppConstants.RequestParam.age] = age
        }
        phoneNo?.let {
            if(it.isNotEmpty())
                params[AppConstants.RequestParam.mobileNo] = phoneNo
        }
        profilePic?.let {
            if(it.isNotEmpty())
                params[AppConstants.RequestParam.profilePic] = profilePic
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


    fun getProfileInfo(userId:String) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.apiForProfileInfo(userId)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    profileInfoLiveData.value = result.value
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

    fun updateProfileInfo() {
        launchDataLoad {
            if(validateData()) {
                showProgressBar()
                val result = repo?.updateProfile(params)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        updateProfileLiveData.value = result.value
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
            params.containsKey(name) && params[name].toString().isBlank() -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.enter_name, FIELD_NAME))
                false
            }
            params.containsKey(mobileNo)
                    && params[mobileNo].toString().isNotBlank()
                    && !isPhoneNumberValid(params[mobileNo].toString())
            -> {
                toastObserver.value =Event(ToastData(MessageConstants.Errors.invalid_phone, FIELD_PHONE))
                false
            }
            params.containsKey(age) && params[age].toString().isNotBlank()
            -> {
                val intAge = params[age].toString().toInt()
                if(intAge<35 || intAge>150) {
                    toastObserver.value = Event(ToastData(MessageConstants.Errors.invalid_age, FIELD_AGE))
                    false
                }else {
                    true
                }
            }
            else -> true
        }
    }
}