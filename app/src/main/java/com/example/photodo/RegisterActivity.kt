package com.example.photodo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope

import com.example.photodo.utils.SecureUtils // 引用加密工具
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [新增] 启用 Edge-to-Edge 模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_register)

        // 防止软键盘反复弹出/收起导致整个 Activity 布局异常移动
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // [新增] 处理 Insets，为系统栏留出空间
        val registerContainer = findViewById<View>(R.id.register_container)
        // 保存原始 padding，避免多次 onApplyWindowInsets 调用导致累加
        val originalPadding = intArrayOf(
            registerContainer.paddingLeft,
            registerContainer.paddingTop,
            registerContainer.paddingRight,
            registerContainer.paddingBottom
        )
        ViewCompat.setOnApplyWindowInsetsListener(registerContainer) { v, insets ->
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

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1. 基础校验
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 数据库操作 (使用协程)
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()

                // 检查用户名是否已存在
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "该用户名已被注册", Toast.LENGTH_SHORT).show()
                } else {
                    // 3. 密码加密处理
                    val salt = SecureUtils.generateSalt() // 生成盐值
                    val passwordHash = SecureUtils.hashPassword(password, salt) // 生成哈希

                    // 4. 创建用户对象
                    val newUser = User(
                        username = username,
                        passwordHash = passwordHash,
                        salt = salt
                    )

                    // 5. 插入数据库
                    userDao.insertUser(newUser)

                    Toast.makeText(this@RegisterActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    finish() // 关闭注册页，返回上一页（通常是登录页）
                }
            }
        }
    }
}