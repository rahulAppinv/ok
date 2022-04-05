package com.app.okra.ui.my_reminder

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import kotlinx.android.synthetic.main.activity_my_reminder.*


class MyReminderActivity : BaseActivity(),View.OnClickListener {

    private lateinit var navHost: NavHostFragment
    private lateinit var navController: NavController

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reminder)
        initialize()
        setViews()
        setListener()
    }

    private fun initialize() {
        navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
    }

    private fun setViews() {
        tvTitle.text = getString(R.string.title_my_reminders)
    }

    public fun setSubTitle(subTitle: String){
        tvSubTitle.text = subTitle
        tvSubTitle.beVisible()
    }

    public fun setSubTitleVisibility(isVisible: Boolean){
        if(isVisible){
            tvSubTitle.beVisible()
        }else{
            tvSubTitle.beGone()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                if(navHost.childFragmentManager.backStackEntryCount == 0 ) {
                    finish()
                }else{
                    navController.popBackStack()
                }
            }
        }
    }

    override fun onBackPressed() {
        onClick(findViewById(R.id.ivBack))
    }

}