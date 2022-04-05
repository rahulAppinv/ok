package com.app.okra.ui.boarding

import android.os.Bundle
import android.view.View
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.navigationOnly
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.boarding.signup.SignUpActivity
import kotlinx.android.synthetic.main.activity_choose.*


class ChooseActivity : BaseActivity(), View.OnClickListener {


    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        makeStatusBarTransparent()
        clNewHere.setOnClickListener (this)
        clHaveBeenBefore.setOnClickListener (this)

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.clNewHere -> {
                navigationOnly(SignUpActivity())
                finish()

            } R.id.clHaveBeenBefore -> {
                navigationOnly(LoginActivity())
            finish()

        }
        }

    }


}