package com.example.photodo.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AiClient {
    // 硅基流动的 API 地址
    private const val BASE_URL = "https://api.siliconflow.cn/"

    // ⚠️ 你的 API Key (实际开发中建议放在 local.properties，这里为了方便先硬编码)
    // 注意：Bearer 后面有个空格
    const val API_KEY = "Bearer sk-oigpunthgrxqdvjewffuaqrofxhiihlhlikkhcykzoekpjqh"

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 打印日志，方便调试
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // 设置超时，AI 思考比较慢
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: AiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiService::class.java)
    }
}