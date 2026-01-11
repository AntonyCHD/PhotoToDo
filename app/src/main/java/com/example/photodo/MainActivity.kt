package com.example.photodo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.photodo.utils.reduceDragSensitivity

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var btnCenterCamera: View
    private lateinit var mainContainer: View // [新增] 定义根布局

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [新增] 启用 Edge-to-Edge 模式
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // 1. 绑定控件
        mainContainer = findViewById(R.id.main_container) // [新增] 绑定根布局
        bottomNav = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.viewPager)
        btnCenterCamera = findViewById(R.id.btnCenterCamera)

        // [新增] 处理 Insets，为系统栏留出空间
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 仅设置底部 Padding，顶部不设置，让 Fragment 的背景延伸到状态栏
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. 设置 ViewPager 适配器
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        // ✨【核心优化】降低 ViewPager2 的灵敏度 ✨
        viewPager.reduceDragSensitivity(3)

        // 3. [核心修改] 设置中间大按钮的交互逻辑
        // 3.1 短按 -> 拍照
        btnCenterCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 3.2 长按 -> 手动新建日程
        btnCenterCamera.setOnLongClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "进入手动创建模式", Toast.LENGTH_SHORT).show()
            true
        }

        // 4. 【联动逻辑 A】: 页面滑动 -> 更新底部按钮
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // 更新状态栏图标颜色
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                // CalendarFragment (position 1) 是白色背景，需要黑色图标 (true)
                // 其他 Fragment (0, 2, 3) 是紫色背景，需要白色图标 (false)
                controller.isAppearanceLightStatusBars = (position == 1)

                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_todo
                    1 -> bottomNav.selectedItemId = R.id.nav_calendar
                    2 -> bottomNav.selectedItemId = R.id.nav_records
                    3 -> bottomNav.selectedItemId = R.id.nav_about
                }
            }
        })

        // 5. 【联动逻辑 B】: 点击底部按钮 -> 切换页面
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_todo -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_calendar -> {
                    viewPager.currentItem = 1
                    true
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

        // 优化：预加载一页，防止滑动卡顿
        viewPager.offscreenPageLimit = 1
    }

    // 适配器逻辑保持不变
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