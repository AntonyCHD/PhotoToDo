package com.example.photodo.utils // 如果没建utils包，就去掉.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.reflect.Field

/**
 * 降低 ViewPager2 的灵敏度
 * @param sensitivity 灵敏度系数 (数字越大，越难触发横向滑动)。
 * 默认值为 4，意味着需要滑动的距离是平时的 4 倍才会触发翻页。
 */
fun ViewPager2.reduceDragSensitivity(sensitivity: Int = 4) {
    try {
        // 1. 通过反射获取 ViewPager2 内部的 RecyclerView
        val recyclerViewField: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        // 2. 获取 RecyclerView 的 TouchSlop (触摸阈值) 字段
        val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true

        // 3. 获取当前的阈值
        val touchSlop = touchSlopField.get(recyclerView) as Int

        // 4. 将阈值设置得更大 (原值 * 系数)
        // 这样就需要手指滑动更长的距离，ViewPager2 才会开始滚动
        touchSlopField.set(recyclerView, touchSlop * sensitivity)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}