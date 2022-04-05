package com.app.okra.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.app.okra.data.network.ApiManager
import com.app.okra.data.network.ApiService
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.utils.AppConstants
import com.app.okra.utils.navigateToLogin
import com.app.okra.utils.showProgressDialog
import kotlinx.coroutines.*


abstract class BaseActivity :AppCompatActivity() {
    private var progressDialog: AlertDialog? = null

    protected var apiService: ApiService = ApiManager.getRetrofit()
    protected var apiServiceAuth: ApiService = ApiManager.getRetrofitAuth()
    protected var apiServiceCalorieMama: ApiService = ApiManager.getRetrofitCalorieMama()

    abstract fun getViewModel() : BaseViewModel?

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        makeStatusBarTransparent()

    }

    protected fun makeStatusBarTransparent() {
        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }


    fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    protected fun showProgressBar() {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            progressDialog = showProgressDialog(this, true)
            progressDialog?.let { dialog ->
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }
        }
    }

    internal fun setBaseObservers(
        viewModel: BaseViewModel?,
        context: FragmentActivity,
        lifecycleOwner: LifecycleOwner,
        observeToast :Boolean = true,
        observeError :Boolean = true,
        observeProgress :Boolean = true,
    ) {
        viewModel?.apply {

            if(observeError) {
                _errorObserver.observe(lifecycleOwner) {
                    val data = it.getContent()!!
                    showToast(data.message!!)
                    checkAndLogout(data.message)
                }
            }


            _genericErrorObserver.observe(lifecycleOwner) {
                val result = it.getContent()!!

                showToast(result.message)

                if (result.message == sessionMsg) {
                    navigateToLogin(context)
                    finish()
                }
            }

            if(observeToast) {
                _toastObserver.observe(lifecycleOwner) {
                    val data = it.getContent()!!
                    showToast(data.message)
                    checkAndLogout(data.message)
                }
            }

            if(observeProgress) {
                _progressDialog.observe(lifecycleOwner) {
                    showHideProgress(it.getContent()!!.status)
                }
            }
        }
    }


    fun isInternetAvailable(): Boolean{
        var result = false
        val connectivityManager  =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }

    fun showHideProgress(show: Boolean) {
        if (show)
            showProgressBar()
        else
            hideProgressBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
                progressDialog = null
            }
        }
    }

    protected fun hideProgressBar() {
        progressDialog?.let { if (it.isShowing) it.dismiss() }
    }

    internal fun launchDataLoad(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend ((scope: CoroutineScope) -> Unit)
    ) : Job {
        return lifecycleScope.launch(dispatcher) {
            block.invoke(this)
        }
    }

    protected fun checkAndLogout(message: String) :Boolean {
        if (message == sessionMsg) {
            navigateToLogin(this)
            finish()
            return true
        }
        return false
    }


    companion object {
        fun saveDataInPreference(

            name: String? = null,
            profilePicture: String? = null,
            email: String? = null,
            userId: String? = null,
            userType: String? = null,
            accessToken: String? = null,
            userData: String? = null,
            password: String? = null,
            age: String? = null,
            phone: String? = null,
            pushNotificationStatus: Boolean? = null,
            inAppNotification: Boolean? = null,
            isApproved: Boolean? = null,
            isVerify: Boolean? = null,
            bloodGlucoseUnit: String? = null,
        ) {
            bloodGlucoseUnit?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT, it)
            }
            accessToken?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.ACCESS_TOKEN, accessToken)
            }
            isApproved?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_APPROVED, isApproved)
            }
            isVerify?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_VERIFIED, isVerify)
            }

            userId?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.USER_ID, userId)
            }

            userType?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.USER_TYPE, userType)
            }

            email?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.EMAIL_ID, email)
            }
            userData?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.USER_DATA, userData)
            }

            name?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.NAME, name)
            }

            profilePicture?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.PROFILE_PIC, profilePicture)
            }
            password?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.PASSWORD, it)
            }
            age?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.AGE, it)
            }
            phone?.let {
                PreferenceManager.putString(AppConstants.Pref_Key.MOBILE, it)
            }
            pushNotificationStatus?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.PUSH_NOTIFICATION, it)
            }
            inAppNotification?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.IN_APP_NOTIFICATION, it)
            }
        }
    }
}