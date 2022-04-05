package com.app.okra.ui.boarding.signup

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
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.InitialBoardingRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.ui.boarding.forgotPassword.ForgotPassActivity
import com.app.okra.ui.boarding.login.InitialBoardingViewModel
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.boarding.otpVerify.OTPVerifyActivity
import com.app.okra.ui.profile.StaticContentActivity
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.DATA
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.EMAIL
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.FROM_SCREEN
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.PASS
import com.app.okra.utils.CustomTypefaceSpan
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.getPrimaryColor
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.layout_button.*


class SignUpActivity : BaseActivity(), View.OnClickListener, TextWatcher {

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            InitialBoardingViewModel(InitialBoardingRepoImpl(apiService))
        }).get(InitialBoardingViewModel::class.java)
    }


    val signInOptionClick: ClickableSpan = object : ClickableSpan() {
        override fun onClick(v: View) {
            navigationOnly(LoginActivity())
        }
    }
    val termsClick: ClickableSpan = object : ClickableSpan() {
        override fun onClick(v: View) {
            navigate( Intent(this@SignUpActivity, StaticContentActivity::class.java).
                putExtra(AppConstants.Intent_Constant.TYPE, StaticContentActivity.TERMS_AND_CONDITION)
            )
        }
    }
    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
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
        etName.addTextChangedListener(this)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this,observeToast = false)
        viewModel._loginLiveData.observe(this) {

            val boardingResponse = it.data as InitialBoardingResponse
            navigate(Intent(this, OTPVerifyActivity::class.java)
                    .putExtra(FROM_SCREEN, SignUpActivity::class.java.simpleName)
                    .putExtra(DATA,boardingResponse )
                    .putExtra(EMAIL, etEmail.text.toString().trim())
                    .putExtra(PASS, etPassword.text.toString().trim()))
        }

        viewModel._toastObserver.observe(this){
            val data = it.getContent()!!
            showToast(data.message)

            tvErrorEmail.beGone()
            tvErrorPass.beGone()
            tvErrorName.beGone()
            etName.setNormalView(this)
            etEmail.setNormalView(this)
            etPassword.setNormalView(this)

            if(!checkAndLogout(data.message)){

                when(data.type) {
                    InitialBoardingViewModel.FIELD_NAME-> {
                        tvErrorName.text = data.message
                        tvErrorName.beVisible()
                        etName.setErrorView(this)
                    }
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

        btnCommon.text = getString(R.string.btn_sign_up)
        btnCommon.beDisable()
        PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_FIRST_TIME, true)
        etPassword.transformationMethod = PasswordTransformationMethod()
        iv_eye.tag = AppConstants.SHOW_TAG

        val boldTypeface = ResourcesCompat.getFont(this, R.font.axiforma_bold)

        // For SignUp
        val span = SpannableString(tvAlreadyHave.text)
        val startSpan = 25
        span.setSpan(signInOptionClick, startSpan, tvAlreadyHave.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        span.setSpan(
                CustomTypefaceSpan(boldTypeface!!),
            startSpan,
                tvAlreadyHave.text.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        span.setSpan(
                ForegroundColorSpan(getPrimaryColor(this)),
            startSpan, tvAlreadyHave.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        tvAlreadyHave.text = span
        tvAlreadyHave.movementMethod = LinkMovementMethod.getInstance()

        // For terms and condition
        val spanTerms = SpannableString(tvTermsAndCondition.text)
        spanTerms.setSpan(termsClick, 56, tvTermsAndCondition.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spanTerms.setSpan(
            CustomTypefaceSpan(boldTypeface!!),
            56,
            tvTermsAndCondition.text.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spanTerms.setSpan(
            ForegroundColorSpan(getPrimaryColor(this)),
            56, tvTermsAndCondition.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        tvTermsAndCondition.text = spanTerms
        tvTermsAndCondition.movementMethod = LinkMovementMethod.getInstance()
    }


    fun onSkipClick(view: View) {
        showToast(MessageConstants.Messages.work_in_progress)
    }

    fun onForgotPassClick(view: View) {
        navigationOnly(ForgotPassActivity())
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnCommon -> {
                initFields()
                val serialNo =
                    if(etSerialNo.text.toString().trim().isNotEmpty()){
                        etSerialNo.text.toString().trim()
                    }else{
                        null
                    }

                viewModel.setSignUpValue(
                    etName.text.toString().trim(),
                    etEmail.text.toString().trim(),
                    etPassword.text.toString().trim(),
                    serialNo
                )
                viewModel.signUp()
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
        val status = (!etName.text.isNullOrEmpty()
                || !etEmail.text.isNullOrEmpty()
                || !etPassword.text.isNullOrEmpty())
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
        tvErrorName.beGone()
        tvErrorEmail.beGone()
        tvErrorPass.beGone()

        etName.setNormalView(this)
        etEmail.setNormalView(this)
        etPassword.setNormalView(this)
    }
}