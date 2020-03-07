package com.crazy_iter.eresta

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class ViewPagerAdapter(manager: FragmentManager): FragmentStatePagerAdapter(manager) {

    private val mFragmentsList: MutableList<Fragment> = mutableListOf()
    private val mFragmentTitleList: MutableList<String> = mutableListOf()

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentsList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getCount(): Int = mFragmentsList.size

    override fun getItem(position: Int): Fragment = mFragmentsList[position]

    override fun getPageTitle(position: Int): CharSequence? = mFragmentTitleList[position]

}