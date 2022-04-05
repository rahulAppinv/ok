package com.app.okra.ui.logbook.meal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.ui.connected_devices.BluetoothDevicesListFragment
import com.app.okra.ui.logbook.meal.meal_detail.EditMealDetailsFragment

class MealDetailsActivity : BaseActivity() {

    override fun getViewModel(): BaseViewModel? {
        return null
    }
    private lateinit var navHost: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_details)
        navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController
        val bundle = Bundle()
        bundle.putParcelable("data",intent.getParcelableExtra("data"))
        navController.setGraph(navController.graph,bundle)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragment: Fragment = navHost.childFragmentManager.fragments[0]!!

        if (fragment is EditMealDetailsFragment) {
            fragment.checkDataExistence()
        }
    }
}