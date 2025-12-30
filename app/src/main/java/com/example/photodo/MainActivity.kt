package com.example.photodo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.photodo.ui.AboutFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.photodo.utils.reduceDragSensitivity // 记得导入刚才写的扩展函数

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var btnCenterCamera: View // [新增] 定义大按钮变量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 绑定控件
        bottomNav = findViewById(R.id.bottom_navigation)
        viewPager = findViewById(R.id.viewPager)
        btnCenterCamera = findViewById(R.id.btnCenterCamera) // [新增] 绑定中间的大按钮

        // 2. 设置 ViewPager 适配器
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        // ✨【核心优化】降低 ViewPager2 的灵敏度 ✨
        // 设为 4 或 5 比较合适，既能防误触，又能在想翻页时翻得动
        viewPager.reduceDragSensitivity(3)

        // 3. [核心修改] 设置中间大按钮的交互逻辑
        // 3.1 短按 -> 拍照
        btnCenterCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 3.2 长按 -> 手动新建日程 (改进计划 Plan 2)
        btnCenterCamera.setOnLongClickListener {
            // 跳转到编辑页，但不传图片URI，触发 EditTaskActivity 的"手动模式"
            val intent = Intent(this, EditTaskActivity::class.java)
            startActivity(intent)

            // 震动反馈或提示 (可选)
            Toast.makeText(this, "进入手动创建模式", Toast.LENGTH_SHORT).show()

            true // 返回 true 表示消耗了事件，松手后不会再触发短按
        }

        // 4. 【联动逻辑 A】: 页面滑动 -> 更新底部按钮
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_todo
                    1 -> bottomNav.selectedItemId = R.id.nav_calendar
                    // 注意：ViewPager 只有 4 页，所以 index 2 对应的是“记录页”
                    // 我们要把底部菜单的 index 3 (nav_records) 选中
                    // (菜单顺序: 0:ToDo, 1:日历, 2:占位符, 3:记录, 4:关于)
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
                // 注意：中间的 nav_placeholder 不需要在这里处理
                // 因为它在 XML 里设置了 enabled="false"，点不动的，只能点上面的大按钮

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