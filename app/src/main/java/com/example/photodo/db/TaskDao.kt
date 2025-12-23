package com.example.photodo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    // 插入一条数据 (suspend 表示这是一个耗时操作，要在协程里跑)
    @Insert
    suspend fun insert(task: Task)

    // 删除一条数据
    @Delete
    suspend fun delete(task: Task)

    // 查询所有数据
    @Query("SELECT * FROM task_table ORDER BY id DESC")
    suspend fun getAllTasks(): List<Task>
    // 新增：模糊查询日期的任务
    // 为什么要用 LIKE？因为你的日期格式可能是 "2025年12月20日"，
    // 这里的 :dateKeyword 我们会传入 "2025年12月20日"
    @Query("SELECT * FROM task_table WHERE date LIKE '%' || :dateKeyword || '%' ORDER BY id DESC")
    suspend fun getTasksByDate(dateKeyword: String): List<Task>
}