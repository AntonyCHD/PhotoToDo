package com.example.photodo // 确保包名正确

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table") // 定义表名叫 task_table
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 自动生成的主键 ID
    val title: String,
    val date: String,
    val time: String,
    val location: String
)