package com.app.okra.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.Dispatcher

class SplashViewModel : BaseViewModel(){

    private val _navEvent = MutableLiveData<Event<SplashNav>>()
    val navEvent : LiveData<Event<SplashNav>>
        get() =  _navEvent


    fun navigateTo(){
        launchDataLoad {
            delay(5000)

            if(!PreferenceManager.getBoolean(AppConstants.Pref_Key.IS_FIRST_TIME)) {
                _navEvent.value = Event(SplashNav.GoToTutorial)
            }else{
                if (PreferenceManager.getBoolean(AppConstants.Pref_Key.IS_LOGGED_IN)) {
                    // when all steps complete
                    _navEvent.value = Event(SplashNav.GoToHome)
                } else {
                    _navEvent.value = Event(SplashNav.GoToLogin)
                }
            }
        }
    }

    sealed class SplashNav {
        object GoToTutorial :SplashNav()
        object GoToHome :SplashNav()
        object GoToLogin :SplashNav()
    }

}
