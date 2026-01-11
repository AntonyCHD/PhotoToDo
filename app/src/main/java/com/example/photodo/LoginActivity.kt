package com.example.photodo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.example.photodo.utils.SecureUtils
import com.example.photodo.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 检查是否已经登录 (自动登录)
        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return // 结束当前 onCreate，避免加载 UI
        }

        // [新增] 启用 Edge-to-Edge 模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_login)

        // 防止软键盘反复弹出/收起导致整个 Activity 布局异常移动
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // [新增] 处理 Insets，为系统栏留出空间
        val loginContainer = findViewById<View>(R.id.login_container)
        // 保存原始 padding，避免多次 onApplyWindowInsets 调用导致累加
        val originalPadding = intArrayOf(
            loginContainer.paddingLeft,
            loginContainer.paddingTop,
            loginContainer.paddingRight,
            loginContainer.paddingBottom
        )
        ViewCompat.setOnApplyWindowInsetsListener(loginContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 基于原始 padding 计算最终 padding（不累加）
            v.setPadding(
                originalPadding[0],
                systemBars.top + originalPadding[1],
                originalPadding[2],
                systemBars.bottom + originalPadding[3]
            )
            insets
        }

        val etUsername = findViewById<EditText>(R.id.etLoginUsername)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // 点击“去注册”
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 点击“登录”
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                // 2. 查库
                val user = db.userDao().getUserByUsername(username)

                if (user != null) {
                    // 3. 校验密码 (输入密码 + 数据库盐值 vs 数据库哈希)
                    val isValid = SecureUtils.verifyPassword(password, user.salt, user.passwordHash)
                    if (isValid) {
                        // 4. 登录成功：保存 Session 并跳转
                        sessionManager.saveUserSession(user.id)
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "密码错误", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "用户不存在", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 关闭登录页，这样按返回键不会退回登录页，而是直接退出 App
    }
}