package com.app.okra.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel


class MyAccountLocalActivity : BaseActivity() {

    private var navHost: NavHostFragment? =null
    private var navController: NavController? =null

    override fun getViewModel(): BaseViewModel? {
     return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        initialize()
        println("::: onCreate - B" )
    }

    override fun onStart() {
        super.onStart()
        println("::: onStart - B" )
    }
    override fun onRestart() {
        super.onRestart()
        println("::: onRestart - B" )
    }

    override fun onResume() {
        super.onResume()
        println("::: onResume - B" )
    }

    override fun onPause() {
        super.onPause()
        println("::: onPause - B" )
    }

    override fun onStop() {
        super.onStop()
        println("::: onStop - B" )
    }

    override fun onDestroy() {
        super.onDestroy()
        println("::: onDestroy - B" )
    }



    private fun initialize() {
        navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment?
        navController = navHost?.navController



        val navInflater = navHost?.navController?.navInflater!!
        val  graph = navInflater?.inflate(R.navigation.nav_graph_dashboard)

        graph?.let{
            it.startDestination = R.id.profileFragment
            navController?.graph = it
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val fragment: Fragment =  navHost?.childFragmentManager?.fragments?.get(0)!!
        fragment.onActivityResult(requestCode, resultCode, data)

    }



}
