package com.example.photodo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ⬇️ 改动1: 添加 User::class 到 entities 数组
// ⬇️ 改动2: 将 version 从 1 改为 2
@Database(entities = [Task::class, User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    // ⬇️ 改动3: 暴露 UserDao 接口供外部使用
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    // ⬇️ 改动4: 允许“破坏性迁移”。
                    // 当数据库结构发生变化（如版本 1->2）且未提供迁移规则时，Room 会直接清空数据重新建表，防止 App 崩溃。
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}