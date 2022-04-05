package com.app.okra.ui.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.ArrayList

class HomeViewPagerAdapter(
    fm: FragmentManager?,
    private val mContext: Context,
    private val type: Int
) :
    FragmentPagerAdapter(fm!!) {
    private val fragmentList: ArrayList<Fragment> = ArrayList<Fragment>()
    fun addFragment(fragment: Fragment?) {
        if (fragment != null) {
            fragmentList.add(fragment)
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

}