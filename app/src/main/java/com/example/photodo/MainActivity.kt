package com.example.photodo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.photodo.ui.AboutFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    // ❌ 已删除 toolbar 变量声明

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ❌ 已删除 toolbar 的初始化和 setSupportActionBar

        bottomNav = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.viewPager)

        // 设置 Adapter
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        // 【联动逻辑 A】: 页面滑动 -> 更新底部按钮
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_todo
                    1 -> bottomNav.selectedItemId = R.id.nav_calendar
                    2 -> bottomNav.selectedItemId = R.id.nav_records
                    3 -> bottomNav.selectedItemId = R.id.nav_about
                }
                // ❌ 重点：这里原来有 toolbar.title = ... ，现在必须全部删掉！
            }
        })

        // 【联动逻辑 B】: 点击底部按钮 -> 切换页面
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_todo -> {
                    viewPager.currentItem = 0
                    // ❌ 删掉 toolbar.title = ...
                    true
                }
                R.id.nav_calendar -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_camera -> {
                    openCameraAction()
                    false
                }
                R.id.nav_records -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_about -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }

        viewPager.offscreenPageLimit = 1
    }

    private fun openCameraAction() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private inner class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TodoFragment()
                1 -> CalendarFragment()
                2 -> RecordsFragment()
                3 -> AboutFragment()
                else -> TodoFragment()
            }
        }
    }
}