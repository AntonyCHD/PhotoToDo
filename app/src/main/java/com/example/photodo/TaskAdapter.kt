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

    // 用于控制首次出现时的入场动画，避免滑动时重复播放
    private var lastAnimatedPosition = -1

    // 动画模式：0 = NONE, 1 = ONCE, 2 = ALWAYS
    private var animationMode = 0
    private var hasPlayedOnce = false

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

        // 入场动画：只在 animationMode 允许时播放
        try {
            if ((animationMode == 2 || animationMode == 1) && position > lastAnimatedPosition) {
                val anim = android.view.animation.AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.slide_in_bottom
                )
                holder.itemView.startAnimation(anim)
                lastAnimatedPosition = position

                // 如果是 ONCE 模式，播放完最后一个条目后关闭动画模式
                if (animationMode == 1 && lastAnimatedPosition >= itemCount - 1) {
                    animationMode = 0
                    hasPlayedOnce = true
                }
            } else {
                holder.itemView.clearAnimation()
            }
        } catch (e: Exception) {
            // 运行时如果资源缺失或动画失败则静默降级
            holder.itemView.clearAnimation()
        }
    }
    override fun getItemCount() = taskList.size

    // 刷新数据的辅助方法
    fun updateData(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }

    /**
     * 手动触发入场动画（例如页面切换时调用）
     */
    fun playEntrance() {
        animationMode = 2
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    /**
     * 播放一次入场动画（之后不会再次自动播放）
     */
    fun playEntranceOnce() {
        if (hasPlayedOnce) return
        animationMode = 1
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }

    override fun onViewDetachedFromWindow(holder: TaskViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }
}