package com.app.okra.ui.tutorial

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.navigationOnly
import com.app.okra.models.TutorialModel
import com.app.okra.ui.boarding.ChooseActivity
import com.app.okra.ui.boarding.login.LoginActivity
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialsActivity : BaseActivity() {

    private lateinit var mAdapter: DotsAdapter
    private lateinit var myAdapter: ViewPagerFragmentAdapter
    private val dataList = ArrayList<TutorialModel>()

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        setViews()
        prepareData()
        setPager()
        setAdapter()
    }

    private fun setViews() {
        val params = llDots.layoutParams as ViewGroup.MarginLayoutParams
        btnSkip.visibility =View.VISIBLE
        btnNext.visibility =View.VISIBLE
        btnGetStarted.visibility =View.GONE
        params.marginEnd = 60
        params.marginStart = 60
        params.topMargin = 0
        params.bottomMargin = 100
    }

    private fun setAdapter() {
        mAdapter = DotsAdapter(this, dataList)
        rv_dots.adapter = mAdapter
    }

    private fun prepareData() {
        dataList.add(TutorialModel(
                headerText = getString(R.string.tutorial_header_1),
                subText = getString(R.string.tutorial_sub_header_1),
                image = R.mipmap.tutorial_1
        ))
        dataList.add(TutorialModel(
                headerText = getString(R.string.tutorial_header_1),
                subText = getString(R.string.tutorial_sub_header_1),
                image = R.mipmap.tutorial_2
        ))
        dataList.add(TutorialModel(
                headerText = getString(R.string.tutorial_header_1),
                subText = getString(R.string.tutorial_sub_header_1),
                image = R.mipmap.tutorial_3
        ))
        dataList[0].isSelected = true
    }

    private fun setPager() {
        myAdapter = ViewPagerFragmentAdapter(supportFragmentManager, lifecycle)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = myAdapter
        viewPager.setPageTransformer(MarginPageTransformer(1500))
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateList(position)
            }
        })
    }

    private fun updateList(position: Int) {
        dataList[position].isSelected =true
        for ((i, data) in dataList.withIndex()){
            if(i !=position){
                data.isSelected =false
            }
        }
        mAdapter.notifyDataSetChanged()

        val params = llDots.layoutParams as ViewGroup.MarginLayoutParams

        if(position == dataList.size-1){
            btnSkip.visibility =View.GONE
            btnNext.visibility =View.GONE
            btnGetStarted.visibility =View.VISIBLE

            params.marginEnd = 20
            params.marginStart = 20
            params.topMargin = 0
            params.bottomMargin =0
        }else{
            btnSkip.visibility =View.VISIBLE
            btnNext.visibility =View.VISIBLE
            btnGetStarted.visibility =View.GONE
            params.marginEnd = 20
            params.marginStart = 20
            params.topMargin = 0
            params.bottomMargin = 100
        }

    }

    fun onGetStartedClick(view: View) {
        navigationOnly(ChooseActivity())
    }
    fun onNextClick(view: View) {
        if(viewPager.currentItem+1<dataList.size){
            viewPager.currentItem = viewPager.currentItem+1
        }
    }
    fun onSkipClick(view: View) {
        navigationOnly(ChooseActivity())
        finish()
    }


    inner class ViewPagerFragmentAdapter(fm: FragmentManager, lifecycle: Lifecycle)
        : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount(): Int {
            return dataList.size
        }
        override fun createFragment(position: Int): Fragment {
            return TutorialFragment.newInstance(dataList[position], position)
        }
    }
}