package com.example.photodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvRecords)

        // 初始化 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TaskAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // 设置删除事件 (点垃圾桶删除)
        adapter.onDeleteClick = { task ->
            deleteTask(task)
        }
    }

    // 每次切换回这个页面时，都要刷新数据
    override fun onResume() {
        super.onResume()
        loadAllTasks()
    }

    private fun loadAllTasks() {
        // 使用 viewLifecycleOwner.lifecycleScope 避免内存泄漏
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                val db = AppDatabase.getDatabase(ctx)
                val tasks = db.taskDao().getAllTasks()
                adapter.updateData(tasks)
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                val db = AppDatabase.getDatabase(ctx)
                db.taskDao().delete(task)
                loadAllTasks() // 删完刷新
            }
        }
    }
}