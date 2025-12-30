package com.example.photodo.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AiService {
    // 硅基流动的接口地址通常兼容 OpenAI 格式
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") auth: String, // 填 "Bearer sk-..."
        @Body request: ChatRequest
    ): ChatResponse
}