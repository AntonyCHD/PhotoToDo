package com.example.photodo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

// 定义一个简单的数据类来描述"某一天"
data class CalendarDay(
    val date: Date,          // 具体的 Date 对象，用于查数据库
    val weekStr: String,     // 显示用的 "周一"
    val dayStr: String,      // 显示用的 "20"
    var isSelected: Boolean = false // 是否被选中
)

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onDayClick: (Date) -> Unit // 点击回调
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        // 加载刚才画的格子布局
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        // 关键点：为了让7天平分屏幕宽度，我们需要动态设置宽度
        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        // 减去两边的 padding (假设是32dp)，然后除以7
        val itemWidth = (screenWidth - (32 * displayMetrics.density).toInt()) / 7
        view.layoutParams.width = itemWidth

        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        holder.tvWeekDay.text = day.weekStr
        holder.tvDayNumber.text = day.dayStr

        // 处理选中状态的样式
        holder.itemView.isSelected = day.isSelected
        if (day.isSelected) {
            holder.tvWeekDay.setTextColor(Color.WHITE)
            holder.tvDayNumber.setTextColor(Color.WHITE)
        } else {
            holder.tvWeekDay.setTextColor(Color.GRAY)
            holder.tvDayNumber.setTextColor(Color.BLACK)
        }

        // 点击事件
        holder.itemView.setOnClickListener {
            // 更新选中状态
            days.forEach { it.isSelected = false }
            day.isSelected = true
            notifyDataSetChanged() // 刷新界面

            // 告诉 Fragment 被点击了
            onDayClick(day.date)
        }
    }

    override fun getItemCount() = days.size

    // 刷新整个周的数据
    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeekDay: TextView = view.findViewById(R.id.tvWeekDay)
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
    }
}