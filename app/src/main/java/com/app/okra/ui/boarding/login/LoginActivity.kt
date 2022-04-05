package com.app.okra.ui.boarding.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.InitialBoardingRepoImpl
import com.app.okra.extension.*
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.boarding.forgotPassword.ForgotPassActivity
import com.app.okra.ui.boarding.otpVerify.OTPVerifyActivity
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordViewModel
import com.app.okra.ui.boarding.signup.SignUpActivity
import com.app.okra.utils.AppConstants
import com.app.okra.utils.CustomTypefaceSpan
import com.app.okra.utils.MessageConstants
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.etEmail
import kotlinx.android.synthetic.main.activity_login.etPassword
import kotlinx.android.synthetic.main.activity_login.iv_eye
import kotlinx.android.synthetic.main.activity_login.tvErrorEmail
import kotlinx.android.synthetic.main.activity_login.tvErrorPass
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.layout_button.*


class LoginActivity : BaseActivity(), View.OnClickListener, TextWatcher {

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            InitialBoardingViewModel(InitialBoardingRepoImpl(apiService))
        }).get(InitialBoardingViewModel::class.java)
    }
    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        makeStatusBarTransparent()
        setView()
        setObserver()
        setListener()
    }

    private fun setListener() {
        btnCommon.setOnClickListener(this)
        iv_eye.setOnClickListener(this)
        etEmail.addTextChangedListener(this)
        etPassword.addTextChangedListener(this)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this,observeToast = false)
        viewModel._loginLiveData.observe(this) { it ->
            it?.data?.let { it ->

                if(it.isApproved!=null && it.isApproved!!) {
                    //showToast("Login Successfully")
                    PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_LOGGED_IN, true)

                    saveDataInPreference(
                        name = it.name,
                        email = it.email,
                        accessToken = it.accessToken,
                        userType = it.userType,
                        userId = it.userId,
                        password = etPassword.text.toString().trim(),
                        age = it.age,
                        isApproved = it.isApproved,
                        isVerify = it.isVerify,
                        phone = it.mobileNo,
                        profilePicture = it.profilePicture,
                        pushNotificationStatus = it.pushNotificationStatus,
                        bloodGlucoseUnit = it.bloodGlucoseUnit
                    )
                    etEmail.setText("")
                    etPassword.setText("")
                    navigationOnly(DashBoardActivity())
                }else{
                    navigate(
                        Intent(this, OTPVerifyActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.FROM_SCREEN, LoginActivity::class.java.simpleName)
                        .putExtra(AppConstants.Intent_Constant.DATA, it)
                        .putExtra(AppConstants.Intent_Constant.EMAIL, etEmail.text.toString().trim())
                        .putExtra(AppConstants.Intent_Constant.PASS, etPassword.text.toString().trim())
                    )
                }
            }
        }

        viewModel._toastObserver.observe(this){
            val data = it.getContent()!!
            showToast(data.message)

            tvErrorEmail.beGone()
            tvErrorPass.beGone()
            etEmail.setNormalView(this)
            etPassword.setNormalView(this)

            if(!checkAndLogout(data.message)){

                when(data.type) {
                    InitialBoardingViewModel.FIELD_EMAIL-> {
                        tvErrorEmail.text = data.message
                        tvErrorEmail.beVisible()
                        etEmail.setErrorView(this)
                    }
                    InitialBoardingViewModel.FIELD_PASS-> {
                        tvErrorPass.text = data.message
                        tvErrorPass.beVisible()
                        etPassword.setErrorView(this)

                    }
                    else-> {
                        tvErrorPass.text = data.message
                        tvErrorPass.beVisible()
                        etPassword.setErrorView(this)

                    }
                }
            }
        }
    }


    private fun setView() {

        btnCommon.text = getString(R.string.btn_sign_in)
        btnCommon.beDisable()
        PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_FIRST_TIME, true)
        etPassword.transformationMethod = PasswordTransformationMethod()
        iv_eye.tag = AppConstants.SHOW_TAG

        val messageClick: ClickableSpan = object : ClickableSpan() {
            override fun onClick(v: View) {
                navigationOnly(SignUpActivity())
            }
        }

        val boldTypeface = ResourcesCompat.getFont(this, R.font.axiforma_bold)

        val span = SpannableString(tvSignUp.text)
        span.setSpan(messageClick, 23, tvSignUp.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        span.setSpan(
            CustomTypefaceSpan(boldTypeface!!),
            23,
            tvSignUp.text.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
            23, tvSignUp.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        tvSignUp.text = span
        tvSignUp.movementMethod = LinkMovementMethod.getInstance()
    }


    fun onSkipClick(view: View) {
        showToast(MessageConstants.Messages.work_in_progress)
    }

    fun onForgotPassClick(view: View) {
        val intent = Intent(this, ForgotPassActivity::class.java)
        intent.putExtra(AppConstants.Intent_Constant.EMAIL, etEmail.text.toString())
        navigate(intent)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnCommon -> {
                initFields()
                viewModel.setLoginValue(etEmail.text.toString().trim(), etPassword.text.toString().trim())
                viewModel.login()
            }
            R.id.iv_eye -> {
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
        }
    }

    private fun activateButton() {
        val status = !etEmail.text.isNullOrEmpty()
        if(status) {
            btnCommon.beEnable()
        }else{
            btnCommon.beDisable()
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
        tvErrorPass.beGone()
        etPassword.setNormalView(this)

    }
}