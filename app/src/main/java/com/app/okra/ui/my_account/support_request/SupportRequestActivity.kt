package com.app.okra.ui.my_account.support_request

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.ui.connected_devices.BluetoothActivity
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.layout_header.*


class SupportRequestActivity : BaseActivity(), View.OnClickListener {

     var screenType: String?=null

    private lateinit var navHost: NavHostFragment
    private lateinit var navController: NavController

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_request)
        initialize()
        getData()
        setViews()
        setListener()
    }

    private fun initialize() {
        navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController
    }

    private fun getData() {
        screenType = intent.getStringExtra(AppConstants.SCREEN_TYPE)
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
    }

    private fun setViews() {
        tvTitle.text = getString(R.string.title_my_support_request)

        if(screenType == BluetoothActivity::class.java.simpleName){
           val graph = navController.navInflater.inflate(R.navigation.nav_graph_support)

            graph.startDestination = R.id.addSupportRequestFragment
            navController.graph = graph
        }
    }


     fun setTitle(title :String){
        tvTitle.text = title
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