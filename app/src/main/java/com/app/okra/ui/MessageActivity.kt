package com.app.okra.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.okra.R
import com.app.okra.extension.navigationOnly
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordActivity
import com.app.okra.ui.boarding.signup.SignUpActivity
import com.app.okra.ui.profile.ProfileFragment
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {

    private var fromScreen: String? = null
    private  var userName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        fromScreen = intent.getStringExtra(AppConstants.SCREEN_TYPE)
        userName = intent.getStringExtra(AppConstants.Intent_Constant.NAME)
        setView()
    }

    private fun setView() {
        if(fromScreen == ProfileFragment::class.java.simpleName){
            btnSend.text = getString(R.string.btn_ok)
        }else if (fromScreen == SignUpActivity::class.java.simpleName){
            btnSend.text = getString(R.string.lets_get_started_2)

            val textToSet = "${getString(R.string.nice_to_meet_you)}\n\"$userName\""
            tvHeader.text = textToSet

            val textToSetSubHeader = getString(R.string.unverified_account_message)
            tvSubHeader.text = textToSetSubHeader

            ivImage.setImageResource(R.mipmap.get_started)

        }
    }

    fun onSendClick(view: View) {
        when (fromScreen) {
            ResetOrChangePasswordActivity::class.java.simpleName -> {
                finishAffinity();
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }
            SignUpActivity::class.java.simpleName -> {
                finishAffinity();
                navigationOnly(DashBoardActivity())
                finish()
            }
            else -> {
                finish()
            }
        }
    }
}