package com.example.photodo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // 插入数据 (不变)
    @Insert
    suspend fun insert(task: Task)

    // 删除数据 (不变)
    @Delete
    suspend fun delete(task: Task)

    // ✅ 改动1：查询所有任务 -> 必须匹配 userId
    @Query("SELECT * FROM task_table WHERE creator_id = :userId ORDER BY id DESC")
    fun getAllTasksFlow(userId: Int): Flow<List<Task>>

    // ✅ 改动2：按日期模糊查询 -> 必须匹配 userId
    // 逻辑：(日期匹配) AND (用户匹配)
    @Query("SELECT * FROM task_table WHERE date LIKE '%' || :dateKeyword || '%' AND creator_id = :userId ORDER BY id DESC")
    fun getTasksByDateFlow(dateKeyword: String, userId: Int): Flow<List<Task>>
}