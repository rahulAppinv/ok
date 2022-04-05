package com.app.okra.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.okra.R
import com.app.okra.extension.beGone
import com.app.okra.extension.beInvisible
import com.app.okra.ui.my_account.support_request.AddSupportRequestFragment
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.activity_message_2.*
import kotlinx.android.synthetic.main.layout_button.*


class Message2Activity : AppCompatActivity(), View.OnClickListener {

    var fromScreen: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_2)
        fromScreen = intent.getStringExtra(AppConstants.SCREEN_TYPE)
        setView()
        setListener()

    }

    private fun setListener() {
        btnCommon.setOnClickListener(this)
    }

    private fun setView() {
        if(fromScreen == AddSupportRequestFragment::class.java.simpleName){
            btnCommon.text = getString(R.string.btn_ok)
            layout.beInvisible()
            tvHeader.text = getString(R.string.hang_tight)
            tvSubHeader.text = getString(R.string.your_message)
            ivImage.setImageResource(R.mipmap.hang_tight)
        }
    }

    fun onSendClick(view: View) {
        if(fromScreen == AddSupportRequestFragment::class.java.simpleName){
            finish()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnCommon -> {
                if(fromScreen == AddSupportRequestFragment::class.java.simpleName) {
                    finish()
                }
            }
        }
    }
}