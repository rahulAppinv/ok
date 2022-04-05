package com.app.okra.base

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.app.okra.data.network.ApiManager
import com.app.okra.data.network.ApiService
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.navigateToLogin
import com.app.okra.utils.showProgressDialog


@Suppress("COMPATIBILITY_WARNING")
abstract class BaseFragment :Fragment() {

    protected lateinit var navController: NavController
    var builder: AlertDialog.Builder? = null
    private var progressDialog: androidx.appcompat.app.AlertDialog? = null
    protected var apiService: ApiService = ApiManager.getRetrofit()
    protected var apiServiceAuth: ApiService = ApiManager.getRetrofitAuth()

    private var viewModel: BaseViewModel?=null

    abstract fun getViewModel() : BaseViewModel?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        builder= AlertDialog.Builder(activity)
    }

    protected fun setBaseObservers(
        viewModel: BaseViewModel?,
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

                    if (data.message == "Your login session has been expired.") {
                        navigateToLogin(requireActivity())

                        requireActivity().finish()
                    }
                }
            }

            if(observeToast) {
                _toastObserver.observe(lifecycleOwner) {
                    val data = it.getContent()!!
                    showToast(data.message)

                    if (data.message == "Your login session has been expired.") {
                        navigateToLogin(requireActivity())
                        requireActivity().finish()
                    }
                }
            }

            if(observeProgress) {
                _progressDialog.observe(lifecycleOwner) { it ->

                    it?.getContent()?.let {
                        if (it.status) {
                            this@BaseFragment.showProgressBar()
                        } else {
                            this@BaseFragment.hideProgressBar()
                        }
                    }
                }
            }
        }
    }


    fun showToast( msg: String, context: Context?=null){
        if(context==null) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    open fun isInternetAvailable(): Boolean  {
        if(activity!=null) {
            return (activity as BaseActivity).isInternetAvailable()
        }
        return false
    }

    open fun addFragment(layoutResId: Int, fragment: BaseFragment?, tag: String?) {
        childFragmentManager.beginTransaction()
            .replace(layoutResId, fragment!!, tag)
            .addToBackStack(tag)
            .commit()
    }


    open fun showProgressBar() {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            progressDialog = showProgressDialog(requireActivity(), false)
            progressDialog?.let { dialog ->
                dialog.setCancelable(false)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }
        }
    }

    fun showToastErrorOccurred() {
        showToast(MessageConstants.Errors.an_error_occurred)
    }

    open fun hideProgressBar() {
        progressDialog?.let { if (it.isShowing) it.dismiss() }
    }
    protected fun checkAndLogout(message: String) :Boolean {
        if (message == "Your login session has been expired.") {
            navigateToLogin(requireActivity())
            requireActivity().finish()
            return true
        }
        return false
    }

    fun executeOnMainThread(codeToExecute: () -> Any){
        requireActivity().runOnUiThread {
            codeToExecute.invoke()
        }

    }

}