package com.app.okra.ui.splash

import android.content.Intent
import android.os.Bundle
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.boarding.login.LoginActivity
import com.app.okra.ui.tutorial.TutorialsActivity
import java.util.*


class SplashActivity : BaseActivity() {

    private val splashViewModel: SplashViewModel? = SplashViewModel()

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        makeStatusBarTransparent()
        setObserver()
        splashViewModel!!.navigateTo()

    }

    private fun setObserver() {
        splashViewModel!!.navEvent.observe(this) { it ->
            it.let { it ->
                val splashNav: SplashViewModel.SplashNav? = it.getContent()
                splashNav.let {
                    when (it) {

                        SplashViewModel.SplashNav.GoToHome -> {
                            val intent = Intent(this@SplashActivity, DashBoardActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        SplashViewModel.SplashNav.GoToLogin -> {
                            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        SplashViewModel.SplashNav.GoToTutorial -> {
                            val intent = Intent(this@SplashActivity, TutorialsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            println(":::: ELSE")
                        }
                    }
                }
            }
        }
    }

}