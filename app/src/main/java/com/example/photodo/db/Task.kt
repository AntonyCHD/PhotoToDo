package com.example.photodo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "task_table",
    // 定义外键约束：Task 的 creator_id 关联到 User 的 id
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creator_id"],
            onDelete = ForeignKey.CASCADE // 如果用户被删除，其名下的日程也一并删除
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val time: String,
    val location: String,

    // 新增字段：记录创建者的 ID
    // index = true 有助于加快查询速度（因为我们以后会经常根据 creator_id 查询）
    @ColumnInfo(name = "creator_id", index = true)
    val creatorId: Int
)