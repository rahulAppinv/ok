package com.app.okra.ui.boarding.resetPassword

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.app.okra.ui.MessageActivity
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.ResetPasswordRepoImpl
import com.app.okra.extension.*
import com.app.okra.ui.profile.ProfileFragment
import com.app.okra.utils.AppConstants
import com.app.okra.utils.MessageConstants
import kotlinx.android.synthetic.main.activity_reset_or_change_password.*
import kotlinx.android.synthetic.main.activity_reset_or_change_password.etPassword
import kotlinx.android.synthetic.main.activity_reset_or_change_password.iv_eye
import kotlinx.android.synthetic.main.activity_reset_or_change_password.tvErrorPass
import kotlinx.android.synthetic.main.activity_reset_or_change_password.tvHeading
import kotlinx.android.synthetic.main.layout_header.*

class ResetOrChangePasswordActivity : BaseActivity(), View.OnClickListener, TextWatcher {

    private val  viewModel by lazy {

        if(screenType == ProfileFragment::class.java.simpleName){
            ViewModelProvider(this,
                    viewModelFactory {
                        ResetOrChangePasswordViewModel(ResetPasswordRepoImpl(apiServiceAuth))
                    }
            ).get(ResetOrChangePasswordViewModel::class.java)
        }else{
            ViewModelProvider(this,
                    viewModelFactory {
                        ResetOrChangePasswordViewModel(ResetPasswordRepoImpl(apiService))
                    }
            ).get(ResetOrChangePasswordViewModel::class.java)
        }

    }
    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private var email :String? =null
    private var screenType :String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_or_change_password)
        getIntentData()
        setObserver()
        setView()
        setListener()
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)

        etConfirmPassword.addTextChangedListener(this)
        etPassword.addTextChangedListener(this)
    }

    private fun getIntentData() {
        email = intent.getStringExtra(AppConstants.EMAIL)
        screenType = intent.getStringExtra(AppConstants.SCREEN_TYPE)
    }

    private fun setView() {
        btnResendPass.beDisable()

        tvTitle.beGone()
        etPassword.transformationMethod = PasswordTransformationMethod()
        etConfirmPassword.transformationMethod = PasswordTransformationMethod()
        iv_eye.tag = AppConstants.SHOW_TAG
        iv_eye_confirm.tag = AppConstants.SHOW_TAG

        if(screenType == ProfileFragment::class.java.simpleName){
            tvSubHeader.text = getString(R.string.enter_ur_password_change_password)
            etPassword.hint = getString(R.string.current_password)
            btnResendPass.text = getString(R.string.btn_change_password)
            tvHeading.text = getString(R.string.change_password)
            etConfirmPassword.hint = getString(R.string.new_password)
        }else{
            tvSubHeader.text = getString(R.string.enter_ur_password)
            etPassword.hint = getString(R.string.password)
            btnResendPass.text = getString(R.string.resend_pass)
            tvHeading.text = getString(R.string.reset_your_password)
        }
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this, observeToast = false)
        viewModel._resetPassLiveData.observe(this){
            val screen = ResetOrChangePasswordActivity::class.java.simpleName
            if(screenType !=ProfileFragment::class.java.simpleName){
                startActivity(Intent(this, MessageActivity::class.java)
                                .putExtra(AppConstants.SCREEN_TYPE, screen)
                )
            }else {
               showToast(MessageConstants.Messages.password_change_successfully)
            }
            finish()
        }
        viewModel._toastObserver.observe(this){
            val data = it.getContent()!!

            tvErrorPass.beGone()
            tvErrorConfirmPass.beGone()
            etConfirmPassword.setNormalView(this)
            etPassword.setNormalView(this)

            if(!checkAndLogout(data.message)){
                showToast(data.message)

                when(data.type) {
                    ResetOrChangePasswordViewModel.FIELD_1-> {
                        tvErrorPass.text = data.message
                        tvErrorPass.beVisible()
                        etPassword.setErrorView(this)
                    }
                    ResetOrChangePasswordViewModel.FIELD_2-> {
                        tvErrorConfirmPass.text = data.message
                        tvErrorConfirmPass.beVisible()
                        etConfirmPassword.setErrorView(this)

                    }
                    else-> {
                        tvErrorConfirmPass.text = data.message
                        tvErrorConfirmPass.beVisible()
                        etConfirmPassword.setErrorView(this)
                        etPassword.setErrorView(this)
                    }
                }

            }


        }
    }


    fun onPassEyeClick(view: View) {
        if (iv_eye.tag == AppConstants.SHOW_TAG) {
            iv_eye.tag = AppConstants.HIDE_TAG
            iv_eye.setImageResource(R.mipmap.eye_open)
            etPassword.transformationMethod = null
            etPassword.setSelection(etPassword.text!!.length)
        } else if (iv_eye.tag == AppConstants.HIDE_TAG) {
            iv_eye.tag = AppConstants.SHOW_TAG
            iv_eye.setImageResource(R.mipmap.closed_eye)
            etPassword.transformationMethod = PasswordTransformationMethod()
            etPassword.setSelection(etPassword.text!!.length)
        }
    }

    fun onConfirmPassEyeClick(view: View) {
        if (iv_eye_confirm.tag == AppConstants.SHOW_TAG) {
            iv_eye_confirm.tag = AppConstants.HIDE_TAG
            iv_eye_confirm.setImageResource(R.mipmap.eye_open)
            etConfirmPassword.transformationMethod = null
            etConfirmPassword.setSelection(etConfirmPassword.text!!.length)
        } else if (iv_eye_confirm.tag == AppConstants.HIDE_TAG) {
            iv_eye_confirm.tag = AppConstants.SHOW_TAG
            iv_eye_confirm.setImageResource(R.mipmap.closed_eye)
            etConfirmPassword.transformationMethod = PasswordTransformationMethod()
            etConfirmPassword.setSelection(etConfirmPassword.text!!.length)
        }
    }

    fun onResetPassClick(view: View) {
        initFields()
        if(screenType == ProfileFragment::class.java.simpleName){
            viewModel.setValue_changePassword(
                    etPassword.text?.trim().toString(),
                    etConfirmPassword.text?.trim().toString()
            )
            viewModel.changePassApi()
        }else {
            email?.let {
                viewModel.setValue(
                        it,
                        etPassword.text?.trim().toString(),
                        etConfirmPassword.text?.trim().toString()
                )
                viewModel.resetPassApi()
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.ivBack -> {
                finish()
            }
        }
    }

    private fun activateButton() {
        val status = !(etPassword.text.isNullOrEmpty()
                && etConfirmPassword.text.isNullOrEmpty()
                )
        if(status) {
            btnResendPass.beEnable()
        }else{
            btnResendPass.beDisable()
        }
    }


    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        activateButton()
    }

    override fun afterTextChanged(p0: Editable?) {}

    private fun initFields(){
        tvErrorPass.beGone()
        etPassword.setNormalView(this)
        tvErrorConfirmPass.beGone()
        etConfirmPassword.setNormalView(this)

    }
}