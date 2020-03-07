package com.crazy_iter.eresta

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_tips.*

class TipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(TipsFragment(0), "")
        adapter.addFragment(TipsFragment(1), "")
        adapter.addFragment(TipsFragment(2), "")
        tipsVP.adapter = adapter

        tipsVP.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) { }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) { }

            override fun onPageSelected(i: Int) {
                setPoints()
                when (i) {
                    0 -> point1.setBackgroundResource(R.drawable.circle_colored_bg)
                    1 -> point2.setBackgroundResource(R.drawable.circle_colored_bg)
                    2 -> point3.setBackgroundResource(R.drawable.circle_colored_bg)
                }
            }

        })

        nextFAB.setOnClickListener {
            val i = tipsVP.currentItem
            if (i == 2) {
                startApp()
            } else {
                tipsVP.setCurrentItem(i + 1, true)
            }
        }

    }

    private fun setPoints() {
        point1.setBackgroundResource(R.drawable.circle_gray_bg)
        point2.setBackgroundResource(R.drawable.circle_gray_bg)
        point3.setBackgroundResource(R.drawable.circle_gray_bg)
    }

    private fun startApp() {
        StaticsData.saveShared(this, StaticsData.FIRST, "false")
        startActivity(Intent(this, MainApp::class.java))
        finish()
    }

}
