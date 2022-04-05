package com.app.okra.ui.boarding.forgotPassword

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.app.okra.data.repo.ForgotPasswordRepoImpl
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.*
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.boarding.otpVerify.OTPVerifyActivity
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordViewModel
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import kotlinx.android.synthetic.main.activity_reset_or_change_password.*
import kotlinx.android.synthetic.main.layout_button.*


class ForgotPassActivity : BaseActivity(), TextWatcher {

    private val  viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                ForgotPasswordViewModel(ForgotPasswordRepoImpl(apiService))
            }
        ).get(ForgotPasswordViewModel::class.java)
    }

    private var email = ""
    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)
        makeStatusBarTransparent()
        btnSend.beDisable()
        getData()
        setView()
        setObserver()
        setListener()
    }

    private fun setView() {
        if(email.isNotEmpty()) {
            etEmail.setText(email ?: "")
            btnSend.beEnable()
        }
    }

    private fun getData() {
        intent?.let {
            if(intent.hasExtra(AppConstants.Intent_Constant.EMAIL)){
                email = intent.getStringExtra(AppConstants.Intent_Constant.EMAIL).toString()
            }
        }
    }

    private fun setListener() {

        etEmail.addTextChangedListener(this)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this, observeToast = false)
        viewModel._fPasswordLiveData.observe(this){
            showToast(getString(R.string.verification_code_successfully_sent_to_your_mail))
            startActivity(
                Intent(this, OTPVerifyActivity::class.java)
                    .putExtra(AppConstants.Intent_Constant.EMAIL, etEmail.text.toString().trim())
                    .putExtra(AppConstants.Intent_Constant.FROM_SCREEN, ForgotPassActivity::class.java.simpleName)

            )
        }

        viewModel._toastObserver.observe(this){
            val data = it.getContent()!!
            showToast(data.message)

            tvErrorEmail.beGone()
            etEmail.setNormalView(this)

            if(!checkAndLogout(data.message)){
                tvErrorEmail.text = data.message
                tvErrorEmail.beVisible()
                etEmail.setErrorView(this)
            }
        }
    }


    fun onSendClick(view: View) {
        initFields()
        viewModel.setValue(etEmail.text.toString().trim())
        viewModel.forgotPassApi()

    }
    fun onBackClick(view: View) {
        finish()
    }


    private fun activateButton() {
        val status = !etEmail.text.isNullOrEmpty()
        if(status) {
            btnSend.beEnable()
        }else{
            btnSend.beDisable()
        }
    }


    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        activateButton()
    }

    override fun afterTextChanged(p0: Editable?) {}

    private fun initFields(){
        tvErrorEmail.beGone()
        etEmail.setNormalView(this)
    }
}