package com.app.okra.ui.logbook.test

import android.os.Bundle
import androidx.navigation.findNavController
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel

class TestDetailsActivity : BaseActivity() {

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_details)
        val navController =findNavController(R.id.container)
        val bundle = Bundle()
        bundle.putParcelable("data",intent.getParcelableExtra("data"))
        navController.setGraph(navController.graph,bundle)
    }
}