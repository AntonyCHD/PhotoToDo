package com.example.photodo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 注册用户：插入新用户
    @Insert
    suspend fun insertUser(user: User): Long

    // 登录检查：根据用户名查找用户（用于验证是否存在或获取密码哈希）
    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // 获取当前用户信息：根据 ID 查找
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?
}