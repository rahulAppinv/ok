package com.app.okra.ui.boarding.otpVerify


import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.app.okra.data.repo.OTPVerifyRepoImpl
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.extension.navigate
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.InitialBoardingResponse
import com.app.okra.ui.MessageActivity
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordActivity
import com.app.okra.ui.boarding.signup.SignUpActivity
import com.app.okra.utils.AppConstants
import com.app.okra.utils.MessageConstants
import kotlinx.android.synthetic.main.activity_otp_verify.*

class OTPVerifyActivity : BaseActivity() {

    private  var screenType: String?=null
    private val viewModel: OTPVerifyViewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                OTPVerifyViewModel(OTPVerifyRepoImpl(apiService))
            }
        ).get(OTPVerifyViewModel::class.java)
    }

    private var userData : InitialBoardingResponse?=null

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verify)
        makeStatusBarTransparent()
        getIntentData()
        manageView()
        setObserver()
        setListener()
    }

    private fun getIntentData() {

        if(intent.hasExtra(AppConstants.Intent_Constant.DATA)){
            userData = intent.getParcelableExtra(AppConstants.Intent_Constant.DATA)
        }
        if(intent.hasExtra(AppConstants.Intent_Constant.FROM_SCREEN)){
            screenType = intent.getStringExtra(AppConstants.Intent_Constant.FROM_SCREEN).toString()
        }

        if(intent.hasExtra(AppConstants.Intent_Constant.EMAIL))
            email = intent.getStringExtra(AppConstants.Intent_Constant.EMAIL).toString()

        if(intent.hasExtra(AppConstants.Intent_Constant.PASS))
            password = intent.getStringExtra(AppConstants.Intent_Constant.PASS).toString()

    }

    private fun manageView() {
        val subText = "${getString(R.string.we_have_sent_a)} $email.\n${getString(R.string.verify_your_otp)} "
        tvSubHeader.text = subText

        val span = SpannableString(tvResendCode.text)
        span.setSpan(UnderlineSpan(), 0, tvResendCode.text.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        tvResendCode.text = span
        tvResendCode.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setObserver() {
        viewModel._OtpVerifyLiveData.observe(this) {it1->
            screenType?.let {
                if(screenType ==  SignUpActivity::class.java.simpleName
                    || screenType== LoginActivity::class.java.simpleName){

                    PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_LOGGED_IN, true)

                    userData?.let {
                        saveDataInPreference(
                            name = it.name,
                            email = it.email,
                            accessToken = it1.data!!.accessToken,
                            userType = it.userType,
                            userId = it.userId,
                            password = password,
                            age = it.age,
                            phone = it.mobileNo,
                            isApproved = true,
                            isVerify = false,
                            profilePicture = it.profilePicture,
                            pushNotificationStatus = it.pushNotificationStatus
                        )
                    }

                    navigate(
                        Intent(this, MessageActivity::class.java)
                            .putExtra(AppConstants.Intent_Constant.NAME, userData?.name)
                            .putExtra(AppConstants.SCREEN_TYPE,
                                SignUpActivity::class.java.simpleName
                            )
                    )
                }else{
                    navigate(
                        Intent(this, ResetOrChangePasswordActivity::class.java)
                            .putExtra(AppConstants.EMAIL, email)
                            .putExtra(
                                AppConstants.SCREEN_TYPE,
                                OTPVerifyActivity::class.java.simpleName
                            )

                    )
                }
                finish()
            }

        }

        viewModel._OtpResendLiveData.observe(this) { it ->
            val apiSuccess: ApiData<*> = it as ApiData<*>

            if (apiSuccess.statusCode == "200") {
                showToast(it.message!!)
            } else {
                apiSuccess.message?.let { showToast(it) }
            }
        }
        viewModel._errorObserver.observe(this) { it ->
            it.getContent()?.let {
                showToast(it.message!!)
                if (it.statusCode == "400"){
                    tvInvalidCode.visibility = View.VISIBLE
                    otp_view.setItemBackground(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.mipmap.otp_error,
                            null
                        )
                    )
                } else {
                    otp_view.setItemBackground(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.mipmap.otp,
                            null
                        )
                    )

                    tvInvalidCode.visibility = View.GONE
                }
            }

        }

        viewModel._toastObserver.observe(this) { it ->
            it.getContent()?.let {
                showToast(it.message)
                if (it.message == MessageConstants.Errors.invalid_otp) {
                    tvInvalidCode.visibility = View.VISIBLE
                    otp_view.setItemBackground(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.mipmap.otp_error,
                            null
                        )
                    )
                } else {
                    otp_view.setItemBackground(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.mipmap.otp,
                            null
                        )
                    )

                    tvInvalidCode.visibility = View.GONE
                }
            }
        }

        viewModel._progressDialog.observe(this) {
            showHideProgress(it.getContent()!!.status);
        }
    }

    private fun setListener() {
        //tv_resend_code.setOnClickListener(this)
    }


    fun onOtpVerifyClick(view: View) {
        /* viewModel.setVerifyOtpValue(phoneNumber!!, email!!, otp_view.text.toString(), screenType!!)
         viewModel.otpVerifyApi()*/
    }

    fun onOTPVerifyBackClick(view: View) {
        finish()
    }

    fun onBackClick(view: View) {
        finish()
    }
    fun onVerifyClick(view: View) {
        viewModel.setVerifyOtpValue(email, otp_view.text.toString())
        viewModel.otpVerifyApi()
    }

    fun onResendClick(view: View) {
        otp_view.setText("")
        viewModel.setResetOTPValue( email, "EMAIL")
        viewModel.otpReSendApi()
    }
}
