package com.example.photodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // 必须导入这个
import java.util.Date             // 必须导入这个
import java.util.Locale           // 必须导入这个

class TodoFragment : Fragment() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTodayDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTodayDate = view.findViewById(R.id.tvTodayDate)
        recyclerView = view.findViewById(R.id.rvTodo)

        // 1. 设置头部显示的日期 (保持中文格式，给用户看)
        // 这里返回的是 "2025年12月22日"
        val displayStr = ImageUtils.getTodayDateString()
        tvTodayDate.text = "今天是：$displayStr"

        // 2. 初始化列表
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TaskAdapter(mutableListOf())
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadTodayTasks()
    }

    private fun loadTodayTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                // --- 核心修改开始 ---

                // 1. 我们不再使用 ImageUtils.getTodayDateString() 来查询
                // 而是现场生成一个 "yyyy-MM-dd" 标准格式的日期字符串
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val queryDate = sdf.format(Date())
                // queryDate 现在是 "2025-12-22"

                // 2. 查数据库
                // 数据库里存的是 "2025-12-22"，现在关键词匹配了！
                val db = AppDatabase.getDatabase(ctx)
                val tasks = db.taskDao().getTasksByDate(queryDate)

                // --- 核心修改结束 ---

                // 3. 更新列表
                adapter.updateData(tasks)
            }
        }
    }
}