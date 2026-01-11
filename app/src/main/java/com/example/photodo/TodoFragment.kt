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
import com.example.photodo.utils.SessionManager // 别忘了导入这个
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoFragment : Fragment() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTodayDate: TextView
    private lateinit var tvWelcome: TextView // ⬇️ 1. 定义变量

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 绑定控件
        tvWelcome = view.findViewById(R.id.tvWelcome) // ⬇️ 2. 绑定控件
        tvTodayDate = view.findViewById(R.id.tvTodayDate)
        recyclerView = view.findViewById(R.id.rvTodo)

        // 设置日期
        val displayStr = ImageUtils.getTodayDateString()
        tvTodayDate.text = "今天是：$displayStr"

        // 初始化列表
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TaskAdapter(mutableListOf())
        adapter.onDeleteClick = { task ->
            deleteTask(task)
        }
        recyclerView.adapter = adapter

        // ⬇️ 3. 调用更新用户信息的方法
        updateUserInfo()

        // 启动观察数据
        loadTodayTasks()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) adapter.playEntrance()
    }

    

    // ⬇️ 4. 新增：查询当前用户名并显示
    private fun updateUserInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                val sessionManager = SessionManager(ctx)
                val userId = sessionManager.getCurrentUserId()

                // 查数据库获取用户详情
                val db = AppDatabase.getDatabase(ctx)
                val currentUser = db.userDao().getUserById(userId)

                if (currentUser != null) {
                    // 更新 UI：显示用户名
                    tvWelcome.text = "你好，${currentUser.username}"
                } else {
                    tvWelcome.text = "你好，同学"
                }
            }
        }
    }

    private fun loadTodayTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val queryDate = sdf.format(Date())

                val sessionManager = SessionManager(ctx)
                val userId = sessionManager.getCurrentUserId()

                val db = AppDatabase.getDatabase(ctx)
                db.taskDao().getTasksByDateFlow(queryDate, userId).collect { tasks ->
                    adapter.updateData(tasks)
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                AppDatabase.getDatabase(ctx).taskDao().delete(task)
                android.widget.Toast.makeText(ctx, "已删除", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}