//这个类负责管理“登录状态”。当用户登录成功后，我们把他的 ID 存起来；
//之后的任何操作（比如查日程），都要先问问这个类“现在是谁在操作”

package com.example.photodo.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "current_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // 保存登录状态
    fun saveUserSession(userId: Int) {
        val editor = prefs.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    // 获取当前用户 ID (如果没有登录，默认返回 -1)
    fun getCurrentUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    // 检查是否已登录
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // 退出登录 (清除数据)
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}