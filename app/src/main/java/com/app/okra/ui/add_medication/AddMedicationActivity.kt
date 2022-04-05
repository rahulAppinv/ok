package com.app.okra.ui.add_medication

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.models.MedicationData
import com.app.okra.ui.logbook.medication.MedicationLogsFragment
import com.app.okra.utils.AppConstants

class AddMedicationActivity : BaseActivity() {

    private var screenFrom :String? = null
    private var medicationData : MedicationData? = null

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    private var navHost: NavHostFragment? = null
    private var navController: NavController? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)
        initializeView()
        getData()
        manageNavigation()
    }

    private fun initializeView() {
        navHost= supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment?
        navController = navHost?.navController
    }

    private fun manageNavigation() {
        screenFrom?.let{
            if(screenFrom.equals(MedicationLogsFragment::class.java.simpleName)){
                val  graph = navHost?.navController?.navInflater!!.inflate(R.navigation.nav_graph_medication)
                graph.apply {
                    startDestination = R.id.medicationDetail
                    navController?.graph = graph

                    if(medicationData!=null) {
                        val bundle = Bundle()
                        bundle.putParcelable(AppConstants.DATA, medicationData)
                        bundle.putString(AppConstants.Intent_Constant.FROM_SCREEN, screenFrom)
                        navController!!.navigate(R.id.medicationDetail,bundle)
                    }else{
                        navController!!.navigate(R.id.medicationDetail)
                    }
                }

            }
        }
    }

    private fun getData() {
        intent?.let{
            if(intent.hasExtra(AppConstants.Intent_Constant.FROM_SCREEN)){
                screenFrom = intent.getStringExtra(AppConstants.Intent_Constant.FROM_SCREEN)
            }
            if(intent.hasExtra(AppConstants.DATA)){
                medicationData = intent.getParcelableExtra(AppConstants.DATA)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val fragment: Fragment =  navHost?.childFragmentManager?.fragments?.get(0)!!
        fragment.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if(navHost!!.childFragmentManager.backStackEntryCount <2 ) {
            finish()
        }else{
            navController!!.popBackStack()
        }
    }
}