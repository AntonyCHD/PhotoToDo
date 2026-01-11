package com.example.photodo.utils

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object SecureUtils {

    // 1. 生成随机盐值 (Salt)
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        // 转为 Base64 字符串方便存入数据库
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    // 2. 将明文密码 + 盐值 -> 转换为哈希值
    fun hashPassword(password: String, salt: String): String {
        val combined = password + salt
        val bytes = combined.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        // 转为 Base64 字符串方便存入数据库
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    // 3. 验证密码 (输入明文 vs 数据库里的 Hash)
    fun verifyPassword(inputPassword: String, salt: String, storedHash: String): Boolean {
        val calculatedHash = hashPassword(inputPassword, salt)
        return calculatedHash == storedHash
    }
}