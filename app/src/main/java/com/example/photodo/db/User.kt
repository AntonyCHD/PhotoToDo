package com.example.photodo

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    // 存储加密后的哈希值，而不是明文密码 [cite: 93]
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    // 存储随机盐值，用于增强密码安全性 [cite: 106]
    @ColumnInfo(name = "salt")
    val salt: String
)