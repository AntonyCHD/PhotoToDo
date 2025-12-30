package com.example.photodo.api

// 1. 发送给 AI 的请求体
data class ChatRequest(
    val model: String = "Qwen/Qwen3-8B", // 模型名称，稍后可改
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 512
)

data class Message(
    val role: String, // "user" 或 "system"
    val content: String
)

// 2. AI 返回的响应体
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message,
    val finish_reason: String?
)