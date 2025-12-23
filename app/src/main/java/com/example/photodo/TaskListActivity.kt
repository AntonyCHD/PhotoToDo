package com.example.photodo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope // 记得导入
import kotlinx.coroutines.launch // 记得导入

class TaskListActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recyclerView = findViewById(R.id.recyclerView)

        // 1. 初始化 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // 2. 设置删除功能 (锦上添花)
        adapter.onDeleteClick = { task ->
            deleteTask(task)
        }

        // 3. 从数据库加载数据
        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@TaskListActivity)
            val tasks = db.taskDao().getAllTasks()
            // 把查到的数据交给 Adapter 显示
            adapter.updateData(tasks)
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@TaskListActivity)
            db.taskDao().delete(task) // 删库
            loadTasks() // 重新查询刷新列表
        }
    }

    // 当回到这个页面时，自动刷新一下（防止刚添加完回来没显示）
    override fun onResume() {
        super.onResume()
        loadTasks()
    }
}