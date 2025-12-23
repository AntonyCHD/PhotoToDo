package com.example.photodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 适配器需要接收一个数据列表
class TaskAdapter(private val taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // 定义点击事件的回调（比如点击删除）
    var onDeleteClick: ((Task) -> Unit)? = null

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvTime: TextView = view.findViewById(R.id.tvItemTime)
        val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // 绑定数据
        holder.tvTitle.text = task.title
        holder.tvTime.text = "📅 ${task.date} ${task.time}"
        holder.tvLocation.text = "📍 ${task.location}"

        // 绑定点击事件
        holder.ivDelete.setOnClickListener {
            onDeleteClick?.invoke(task)
        }
    }

    override fun getItemCount() = taskList.size

    // 刷新数据的辅助方法
    fun updateData(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }
}