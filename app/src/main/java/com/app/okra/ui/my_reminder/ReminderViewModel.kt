package com.app.okra.ui.my_reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.ReminderRepo
import com.app.okra.models.UserDetailResponse
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants

class ReminderViewModel(private val repo: ReminderRepo?) : BaseViewModel() {

    private var setReminderLiveData = MutableLiveData<ApiData<Any>>()
    val _setReminderLiveData: LiveData<ApiData<Any>>
        get() = setReminderLiveData

    private var profileInfoLiveData = MutableLiveData<ApiData<UserDetailResponse>>()
    val _profileInfoLiveData: LiveData<ApiData<UserDetailResponse>>
        get() = profileInfoLiveData

    fun setReminder(data : HashMap<String,Any>) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.setReminder(data)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    setReminderLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = MessageConstants.Errors.network_issue))
                }
            }
        }
    }

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

     fun getRepeatType(repeatType:String, forNotification:Boolean =false):String {
        return if(forNotification) {
            when (repeatType) {
                AppConstants.MONTHLY -> AppConstants.MONTHLY
                AppConstants.DAILY -> AppConstants.DAILY
                AppConstants.WEEKLY -> AppConstants.WEEKLY
                else -> AppConstants.NEVER_TEXT
            }
        }else{
            when (repeatType) {
                AppConstants.NEVER_TEXT -> AppConstants.NEVER
                AppConstants.DAILY -> AppConstants.EVERY_DAY
                AppConstants.MONTHLY -> AppConstants.EVERY_MONTH
                AppConstants.WEEKLY -> AppConstants.EVERY_WEEK
                else -> AppConstants.SET_UP
            }

        }
    }
}