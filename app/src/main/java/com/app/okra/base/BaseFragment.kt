package com.app.okra.base

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.app.okra.data.network.ApiManager
import com.app.okra.data.network.ApiService
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.MessageConstants.Messages.Companion.sessionMsg
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
        setErrorObserver(viewModel,observeError)
        setToastObserver(viewModel,observeToast)
        setProgressObserver(viewModel, observeProgress)
    }

    private fun setProgressObserver(_viewModel: BaseViewModel?, observeProgress: Boolean) {
        if(observeProgress) {
            _viewModel?._progressDialog?.observe(viewLifecycleOwner) { it ->
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

    private fun setToastObserver(_viewModel: BaseViewModel?, observeToast: Boolean) {
        if(observeToast) {
            _viewModel?._toastObserver?.observe(viewLifecycleOwner) {
                val data = it.getContent()!!
                showToast(data.message)

                if (data.message == sessionMsg) {
                    navigateToLogin(requireActivity())
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setErrorObserver(_viewModel: BaseViewModel?, observeError: Boolean) {
        if (observeError) {
            _viewModel?._errorObserver?.observe(viewLifecycleOwner) {
                val data = it.getContent()!!

                if (data.message == sessionMsg) {
                    navigateToLogin(requireActivity())

                    requireActivity().finish()
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
        if (message == sessionMsg) {
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