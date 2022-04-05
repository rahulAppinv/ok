package com.app.okra.base

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.app.okra.data.network.ApiManager
import com.app.okra.data.network.ApiService
import com.app.okra.utils.AppConstants
import com.app.okra.utils.navigateToLogin
import com.app.okra.utils.showProgressDialog

abstract class BaseFragmentWithoutNav : Fragment() {

    var builder: AlertDialog.Builder? = null
    private var progressDialog: androidx.appcompat.app.AlertDialog? = null
    protected var apiService: ApiService = ApiManager.getRetrofit()
    protected var apiServiceAuth: ApiService = ApiManager.getRetrofitAuth()

    private var viewModel: BaseViewModel? = null

    abstract fun getViewModel(): BaseViewModel?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        builder = AlertDialog.Builder(activity)
        setObserver()
    }

    private fun setObserver() {
        viewModel?.apply {
            _errorObserver.observe(viewLifecycleOwner) {
                val data = it.getContent()!!
                showToast(data.message!!)

                if (data.message == "Your login session has been expired.") {
                    navigateToLogin(requireActivity())

                    requireActivity().finish()
                }
            }

            _toastObserver.observe(viewLifecycleOwner) {
                val data = it.getContent()!!
                showToast(data.message)

                if (data.message == "Your login session has been expired.") {
                    navigateToLogin(requireActivity())

                    requireActivity().finish()
                }
            }

            _progressDialog.observe(viewLifecycleOwner) { it ->

                it?.getContent()?.let {
                    if (it.status) {
                        this@BaseFragmentWithoutNav.showProgressBar()
                    } else {
                        this@BaseFragmentWithoutNav.hideProgressBar()
                    }
                }
            }
        }
    }


    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    open fun isInternetAvailable(): Boolean {
        if (activity != null) {
            return (activity as BaseActivity).isInternetAvailable()
        }
        return false
    }

    open fun addFragment(layoutResId: Int, fragment: BaseFragment?, tag: String?) {
        if (childFragmentManager.findFragmentByTag(tag) == null) childFragmentManager.beginTransaction()
            .add(layoutResId, fragment!!, tag)
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

    open fun hideProgressBar() {
        progressDialog?.let { if (it.isShowing) it.dismiss() }
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
                            this@BaseFragmentWithoutNav.showProgressBar()
                        } else {
                            this@BaseFragmentWithoutNav.hideProgressBar()
                        }
                    }
                }
            }
        }
    }

}