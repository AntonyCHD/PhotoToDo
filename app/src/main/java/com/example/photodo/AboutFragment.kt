package com.example.photodo// 确保包名与你的目录结构一致 (如果不在此包下，请修改为 package com.example.photodo)

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.photodo.LoginActivity
import com.example.photodo.R
import com.example.photodo.utils.SessionManager // 确保引入 SessionManager

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 找到退出按钮
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // 2. 设置点击事件
        btnLogout.setOnClickListener {
            // 初始化 SessionManager
            val sessionManager = SessionManager(requireContext())

            // 执行退出：清除保存的用户ID
            sessionManager.logout()

            // 跳转回登录页
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // 清空任务栈 (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)
            // 这样用户点击“返回”按钮时，不会回到主页，而是直接退出 App
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}