package com.example.photodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    // 2. 修改刷新逻辑，使用 Flow (需新增一个 Job 变量来管理，防止多次订阅冲突)
    private var searchJob: kotlinx.coroutines.Job? = null // 在类顶部定义
    private lateinit var rvCalendar: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter

    private lateinit var rvTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPrevWeek: Button
    private lateinit var btnNextWeek: Button
    private lateinit var btnBackToday: Button

    // 当前选中的日期（默认今天）
    private var selectedDate: Date = Date()
    // 当前显示的周的基准日期（用于计算那一周的7天）
    private var currentWeekBaseDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 绑定控件
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        btnPrevWeek = view.findViewById(R.id.btnPrevWeek)
        btnNextWeek = view.findViewById(R.id.btnNextWeek)
        btnBackToday = view.findViewById(R.id.btnBackToday)
        rvCalendar = view.findViewById(R.id.rvCalendar)
        rvTasks = view.findViewById(R.id.rvTasks)

        // 1. 初始化下面的任务列表
        rvTasks.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(mutableListOf())
        rvTasks.adapter = taskAdapter


        // ✅ 新增：允许在日历页删除
        taskAdapter.onDeleteClick = { task ->
            deleteTask(task)
        }
        // 2. 初始化上面的日历列表
        rvCalendar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        calendarAdapter = CalendarAdapter(emptyList()) { clickedDate ->
            // 当点击某一天时：
            selectedDate = clickedDate
            refreshTaskList(clickedDate)
        }
        rvCalendar.adapter = calendarAdapter

        // 3. 按钮点击事件
        btnPrevWeek.setOnClickListener {
            changeWeek(-7) // 减7天
        }
        btnNextWeek.setOnClickListener {
            changeWeek(7) // 加7天
        }
        btnBackToday.setOnClickListener {
            // 回到今天
            val today = Date()
            selectedDate = today
            currentWeekBaseDate = today
            updateCalendarUI()
        }

        // 4. 首次加载
        updateCalendarUI()
    }

    /**
     * 核心算法：根据 currentWeekBaseDate 计算出一周7天的数据
     */
    private fun updateCalendarUI() {
        // 1. 计算本周的第一天（周一）
        val calendar = Calendar.getInstance(Locale.CHINA)
        calendar.time = currentWeekBaseDate

        // 设置一周的第一天是周一 (中国习惯)
        calendar.firstDayOfWeek = Calendar.MONDAY
        // 调整到本周的周一
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // 2. 循环生成7天的数据
        val weekDays = mutableListOf<CalendarDay>()
        val sdfWeek = SimpleDateFormat("E", Locale.CHINA) // "周一"
        val sdfDay = SimpleDateFormat("d", Locale.CHINA)  // "20"

        // 记录一下这周第一天，用来更新顶部的 "2025年12月"
        val firstDayOfWeek = calendar.time

        for (i in 0 until 7) {
            val date = calendar.time
            val isSelected = isSameDay(date, selectedDate)

            weekDays.add(CalendarDay(
                date = date,
                weekStr = sdfWeek.format(date),
                dayStr = sdfDay.format(date),
                isSelected = isSelected
            ))

            // 天数+1，准备下一次循环
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 3. 更新日历列表
        calendarAdapter.updateDays(weekDays)

        // 4. 更新顶部标题 (显示 年-月)
        val sdfTitle = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
        tvCurrentMonth.text = sdfTitle.format(firstDayOfWeek)

        // 5. 刷新下面的任务列表 (查询选中那天的任务)
        refreshTaskList(selectedDate)
    }

    private fun changeWeek(amount: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentWeekBaseDate
        calendar.add(Calendar.DAY_OF_MONTH, amount)
        currentWeekBaseDate = calendar.time
        updateCalendarUI()
    }

//    private fun refreshTaskList(date: Date) {
//        // --- 核心修改点 ---
//        // 把 Date 转成 String，必须使用标准格式 "yyyy-MM-dd" 才能匹配数据库
//        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val dateStr = sdfDb.format(date)
//        // ------------------
//
//        // 取消上一次的观察，避免重复订阅
//        searchJob?.cancel()
//
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            context?.let { ctx ->
//                val db = AppDatabase.getDatabase(ctx)
//                // 此时 dateStr 是 "2025-12-22"，与数据库内容完美匹配
////                val tasks = db.taskDao().getTasksByDate(dateStr)
////                taskAdapter.updateData(tasks)
//                db.taskDao().getTasksByDateFlow(dateStr).collect { tasks ->
//                    taskAdapter.updateData(tasks)
//
//            }
//        }
//    }

    private fun refreshTaskList(date: Date) {
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdfDb.format(date)

        // 取消上一次的观察，避免重复订阅
        searchJob?.cancel()

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                val db = AppDatabase.getDatabase(ctx)
                // ✅ 使用 Flow 实时观察
                db.taskDao().getTasksByDateFlow(dateStr).collect { tasks ->
                    taskAdapter.updateData(tasks)
                }
            }
        }
    }


    // 3. 新增删除方法
    private fun deleteTask(task: Task) {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                AppDatabase.getDatabase(ctx).taskDao().delete(task)
                android.widget.Toast.makeText(ctx, "已删除", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 辅助函数：判断两天是不是同一天 (用于UI高亮，不需要改)
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(date1) == sdf.format(date2)
    }
}