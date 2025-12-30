# 改动计划

## 1.新增用户注册与登录功能，以做到分用户管理数据以及日程(ROOM数据库的结构或者需要新建字段需要做改动)

以下为现有的定义`Task.kt`，用户表按照标准并安全的存储方式进行构建，比如存储用户口令时hash并加盐值存储，

```kotlin
package com.example.photodo // 确保包名正确

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table") // 定义表名叫 task_table
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 自动生成的主键 ID
    val title: String,
    val date: String,
    val time: String,
    val location: String
)
```

## 2.新增手动创建日程的功能

大致实现逻辑如下

目前软件的下边栏存在5个按钮，从左到右分别是ToDo 日程 拍照 记录 关于 五个按钮，并且风格都是一致的，现在改为以拍照为中心，左右各排两个按钮，不过拍照按钮改为圆形，该按钮的圆形直径大于左右容纳四个按钮的直径，如下图所示，期望是长按拍照按钮，会在拍照按钮上方出现一个气泡，气泡中是一个加号，手指上滑触碰到该加号时会跳转到手动创建日程的页面，在这个页面用户可以自行编辑日程，而不是像现在这样仅能拍照识别在进行修改

![image-20251229195540939](./image-20251229195540939.png)

## 3. 新增OCR SDK (百度旗下的)在线识别功能(如果可以)

我已下载该工具包，`aip-ocr-android-sdk-2.3.0.zip`，这里我有个疑问，仅下载这个工具包能否直接使用百度旗下的OCR光学字符识别的服务，还是说我仍需要去百度的相关平台申请服务才可调用，如果可以调用，我该怎么使用这个开发工具包，并替换掉原有的Google MT kit(原有的文字识别为离线的，即已经打包进软件中)，这里你自行进行取舍，我其实觉得需要保留，以支持离线识别

## 4.新增人工智能大模型处理识别后的文字功能

目前本软件将识别后的文字转为日程(包含具体日程、时间(包含日期以及时刻)、地点总共四个数据，参见以上定义`Task.kt`的内容)的方式是通过正则表达式提取，如以下代码所示，提取范围有限，功能有限并且大概率提取不准确，因此改为大语言模型进行分析提取

以下是我询问另一个大模型的具体操作
硅基流动的API密钥为`sk-oigpunthgrxqdvjewffuaqrofxhiihlhlikkhcykzoekpjqh`

硅基流动平台的实际模型名称为`Qwen/Qwen3-8B`

># 硅基流动Qwen3-8B模型API集成到Kotlin安卓应用指南
>
>根据您提供的硅基流动API文档，我将为您详细说明如何在Kotlin安卓应用中集成Qwen3-8B模型的调用。以下是针对硅基流动API的完整实现方案：
>
>## 一、确认模型名称
>
>首先，从您提供的API文档截图看，需要确认Qwen3-8B的具体模型名称。在"Available options"列表中没有直接显示Qwen3-8B，但根据硅基流动的命名规范，Qwen3-8B的模型名称可能是：
>- `Qwen/Qwen3-8B` 
>- `Qwen/Qwen3-8B-Chat`
>- `Qwen/Qwen3-8B-Base`
>
>**建议**：登录硅基流动控制台查看可用模型列表，确认Qwen3-8B的准确名称。
>
>## 二、Kotlin实现步骤
>
>### 1. 添加必要的依赖 (build.gradle)
>
>```gradle
>dependencies {
>    // 网络请求
>    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
>    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
>    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
>    implementation 'com.squareup.okhttp3:logging-interceptor:4.9.3'
>    
>    // 协程支持
>    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
>    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
>    
>    // 安全存储
>    implementation 'androidx.security:security-crypto:1.1.0-alpha03'
>}
>```
>
>### 2. 创建API服务接口 (AiApiService.kt)
>
>```kotlin
>import retrofit2.http.*
>
>interface AiApiService {
>    @POST("/v1/chat/completions")
>    @Headers("Content-Type: application/json")
>    suspend fun getChatCompletion(
>        @Header("Authorization") authHeader: String,
>        @Body request: ChatRequest
>    ): ChatResponse
>}
>```
>
>### 3. 定义请求和响应数据类
>
>**ChatRequest.kt**
>```kotlin
>data class ChatRequest(
>    val model: String, // 您需要确认Qwen3-8B的具体名称
>    val messages: List<Message>,
>    val stream: Boolean = false,
>    val max_tokens: Int = 4096,
>    val temperature: Double = 0.7,
>    val top_p: Double = 0.9,
>    val frequency_penalty: Double = 0.0,
>    val presence_penalty: Double = 0.0
>)
>
>data class Message(
>    val role: String,
>    val content: String
>)
>```
>
>**ChatResponse.kt**
>```kotlin
>data class ChatResponse(
>    val id: String,
>    val object: String,
>    val created: Long,
>    val model: String,
>    val choices: List<Choice>,
>    val usage: Usage
>)
>
>data class Choice(
>    val index: Int,
>    val message: Message,
>    val finish_reason: String
>)
>
>data class Usage(
>    val prompt_tokens: Int,
>    val completion_tokens: Int,
>    val total_tokens: Int
>)
>```
>
>### 4. 创建Retrofit实例 (ApiClient.kt)
>
>```kotlin
>import okhttp3.OkHttpClient
>import okhttp3.logging.HttpLoggingInterceptor
>import retrofit2.Retrofit
>import retrofit2.converter.gson.GsonConverterFactory
>
>object ApiClient {
>    private const val BASE_URL = "https://api.siliconflow.cn/"
>    
>    private val loggingInterceptor = HttpLoggingInterceptor().apply {
>        level = HttpLoggingInterceptor.Level.BODY
>    }
>    
>    private val okHttpClient = OkHttpClient.Builder()
>        .addInterceptor(loggingInterceptor)
>        .build()
>    
>    val retrofit: Retrofit = Retrofit.Builder()
>        .baseUrl(BASE_URL)
>        .client(okHttpClient)
>        .addConverterFactory(GsonConverterFactory.create())
>        .build()
>    
>    val apiService: AiApiService = retrofit.create(AiApiService::class.java)
>}
>```
>
>### 5. 安全管理API密钥 (SecureStorage.kt)
>
>**重要**：不要在客户端硬编码API密钥！这里提供两种安全方案：
>
>#### 方案1：使用后端中转（推荐）
>```kotlin
>// 您的后端API服务
>interface BackendApi {
>    @POST("/ai/generate")
>    suspend fun generateResponse(@Body request: UserMessageRequest): AiResponse
>}
>
>// 用户消息请求
>data class UserMessageRequest(
>    val message: String,
>    val model: String = "Qwen/Qwen3-8B" // 您需要确认的模型名称
>)
>```
>
>#### 方案2：使用Android Keystore安全存储（仅用于测试）
>```kotlin
>import android.security.keystore.KeyGenParameterSpec
>import android.security.keystore.KeyProperties
>import java.security.KeyStore
>import javax.crypto.Cipher
>import javax.crypto.KeyGenerator
>import javax.crypto.SecretKey
>import javax.crypto.spec.IvParameterSpec
>
>class SecureStorage(private val context: Context) {
>    private val KEY_ALIAS = "ai_api_key"
>    private val ANDROID_KEYSTORE = "AndroidKeyStore"
>    
>    fun saveApiKey(apiKey: String) {
>        try {
>            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
>            keyStore.load(null)
>            
>            // 创建密钥
>            if (!keyStore.containsAlias(KEY_ALIAS)) {
>                val keyGenerator = KeyGenerator.getInstance(
>                    KeyProperties.KEY_ALGORITHM_AES,
>                    ANDROID_KEYSTORE
>                )
>                val params = KeyGenParameterSpec.Builder(
>                    KEY_ALIAS,
>                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
>                )
>                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
>                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
>                    .setRandomizedEncryptionRequired(false)
>                    .build()
>                
>                keyGenerator.init(params)
>                keyGenerator.generateKey()
>            }
>            
>            // 加密并保存
>            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
>            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
>            val iv = cipher.iv
>            val encrypted = cipher.doFinal(apiKey.toByteArray())
>            
>            // 保存到SharedPreferences
>            val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
>            prefs.edit()
>                .putString("api_key", Base64.encodeToString(encrypted, Base64.DEFAULT))
>                .putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
>                .apply()
>                
>        } catch (e: Exception) {
>            e.printStackTrace()
>        }
>    }
>    
>    fun getApiKey(): String? {
>        // 实现解密逻辑
>        return null
>    }
>    
>    private fun getSecretKey(): SecretKey {
>        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
>        keyStore.load(null)
>        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
>    }
>}
>```
>
>### 6. 创建API调用服务 (AiService.kt)
>
>```kotlin
>import kotlinx.coroutines.Dispatchers
>import kotlinx.coroutines.withContext
>import retrofit2.HttpException
>import java.io.IOException
>
>class AiService {
>    private val apiService = ApiClient.apiService
>    
>    suspend fun generateResponse(
>        message: String,
>        apiKey: String,
>        model: String = "Qwen/Qwen3-8B" // 替换为实际模型名称
>    ): Result<String> {
>        return withContext(Dispatchers.IO) {
>            try {
>                val request = ChatRequest(
>                    model = model,
>                    messages = listOf(Message("user", message))
>                )
>                
>                val response = apiService.getChatCompletion("Bearer $apiKey", request)
>                if (response.choices.isNotEmpty()) {
>                    Result.success(response.choices[0].message.content)
>                } else {
>                    Result.failure(Exception("No response from AI"))
>                }
>            } catch (e: HttpException) {
>                Result.failure(Exception("API error: ${e.code()}"))
>            } catch (e: IOException) {
>                Result.failure(Exception("Network error: ${e.message}"))
>            } catch (e: Exception) {
>                Result.failure(e)
>            }
>        }
>    }
>}
>```
>
>### 7. 在ViewModel中调用API (AiViewModel.kt)
>
>```kotlin
>import androidx.lifecycle.ViewModel
>import androidx.lifecycle.viewModelScope
>import kotlinx.coroutines.flow.MutableStateFlow
>import kotlinx.coroutines.flow.StateFlow
>import kotlinx.coroutines.launch
>
>class AiViewModel : ViewModel() {
>    private val _response = MutableStateFlow<String?>(null)
>    val response: StateFlow<String?> = _response
>    
>    private val _isLoading = MutableStateFlow(false)
>    val isLoading: StateFlow<Boolean> = _isLoading
>    
>    private val _error = MutableStateFlow<String?>(null)
>    val error: StateFlow<String?> = _error
>    
>    private val aiService = AiService()
>    
>    fun sendMessage(message: String, apiKey: String) {
>        _isLoading.value = true
>        _error.value = null
>        
>        viewModelScope.launch {
>            try {
>                val result = aiService.generateResponse(message, apiKey)
>                result.fold(
>                    onSuccess = { _response.value = it },
>                    onFailure = { _error.value = it.message }
>                )
>            } finally {
>                _isLoading.value = false
>            }
>        }
>    }
>}
>```
>
>### 8. 在Activity或Fragment中使用
>
>```kotlin
>class ChatActivity : AppCompatActivity() {
>    private lateinit var binding: ActivityChatBinding
>    private val viewModel: AiViewModel by viewModels()
>    
>    override fun onCreate(savedInstanceState: Bundle?) {
>        super.onCreate(savedInstanceState)
>        binding = ActivityChatBinding.inflate(layoutInflater)
>        setContentView(binding.root)
>        
>        setupObservers()
>        setupClickListeners()
>    }
>    
>    private fun setupObservers() {
>        viewModel.response.observe(this) { response ->
>            if (!response.isNullOrEmpty()) {
>                binding.chatOutput.text = response
>            }
>        }
>        
>        viewModel.isLoading.observe(this) { isLoading ->
>            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
>            binding.sendButton.isEnabled = !isLoading
>        }
>        
>        viewModel.error.observe(this) { error ->
>            if (!error.isNullOrEmpty()) {
>                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
>            }
>        }
>    }
>    
>    private fun setupClickListeners() {
>        binding.sendButton.setOnClickListener {
>            val message = binding.inputMessage.text.toString().trim()
>            if (message.isNotEmpty()) {
>                // 从安全存储获取API密钥
>                val apiKey = getApiKeyFromSecureStorage()
>                if (apiKey != null) {
>                    viewModel.sendMessage(message, apiKey)
>                } else {
>                    Toast.makeText(this, "API key not configured", Toast.LENGTH_SHORT).show()
>                }
>            }
>        }
>    }
>    
>    private fun getApiKeyFromSecureStorage(): String? {
>        // 实际实现应从安全存储获取
>        // 例如：return SecureStorage(this).getApiKey()
>        return "YOUR_API_KEY" // 仅用于测试
>    }
>}
>```
>
>## 三、安全关键点
>
>1. **API密钥管理**：
>   - 生产环境**必须**使用后端中转方案
>   - 通过后端生成临时访问令牌，而不是直接暴露API密钥
>   - 后端可以实现请求频率限制、内容审核等安全措施
>
>2. **请求内容过滤**：
>   ```kotlin
>   fun sanitizeInput(input: String): String {
>       // 移除潜在的恶意内容
>       return input.replace("<script>", "").replace("javascript:", "")
>   }
>   ```
>
>3. **网络权限**：
>   ```xml
>   <uses-permission android:name="android.permission.INTERNET" />
>   ```
>
>## 四、针对硅基流动API的特别说明
>
>1. **模型名称确认**：
>   - 在调用前必须确认Qwen3-8B的准确模型名称
>   - 可通过硅基流动控制台或API获取可用模型列表
>
>2. **请求格式**：
>   - 与OpenAI格式兼容，但基础URL不同
>   - 确保使用正确的端点：`https://api.siliconflow.cn/v1/chat/completions`
>
>3. **错误处理**：
>   - 硅基流动可能返回特定错误码，需要针对性处理
>   - 实现重试机制应对临时性错误
>
>## 五、推荐的生产环境架构
>
>```
>┌─────────────┐     ┌─────────────┐     ┌───────────────────────────┐
>│  Android App│────▶│   Your      │────▶│  SiliconFlow API          │
>│             │     │   Backend   │     │  (Qwen3-8B Model)         │
>└─────────────┘     └─────────────┘     └───────────────────────────┘
>       │                   │
>       │ 1. User Request   │ 3. Sanitized Request
>       │ 2. Display Result │ 4. Processed Response
>       ▼                   ▼
>```
>
>**后端应实现的功能**：
>- API密钥安全管理
>- 请求频率限制
>- 敏感内容过滤
>- 响应缓存
>- 错误处理和重试
>
>## 六、测试建议
>
>1. 先使用Postman测试API，确认Qwen3-8B模型名称和响应格式
>2. 在开发环境中使用测试API密钥
>3. 实现完整的错误处理和用户反馈机制
>4. 测试不同长度的输入和响应
>
>如果您需要更具体的实现细节，或者针对硅基流动API的特定问题，我可以提供更深入的代码示例。请确认Qwen3-8B的具体模型名称，这将确保集成顺利进行。

```kotlin
package com.example.photodo

import java.util.regex.Pattern

object SmartParser {

    /**
     * 核心算法：输入一长串乱七八糟的文字，输出一个整洁的 TaskInfo 对象
     */
    fun parseTextToTask(text: String): TaskInfo {
        val task = TaskInfo()
        task.rawContent = text

        // 1. 提取标题 (简单策略：取第一行有效文本)
        val lines = text.split("\n").filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            // 比如你的例子中，第一行是"亲爱的同学们..."，第二行是"第四十一届..."
            // 我们可以取字数较长且包含"大赛"、"会议"、"通知"的那一行，或者直接取前两行拼起来
            task.title = lines.find { it.contains("大赛") || it.contains("活动") } ?: lines[0]
        }

        // 2. 提取日期 (正则：匹配 2025年12月19日 或 12月19日)
        // \d{4} 表示4个数字，\d{1,2} 表示1到2个数字
        val datePattern = Pattern.compile("(\\d{4}年)?\\d{1,2}月\\d{1,2}日")
        val dateMatcher = datePattern.matcher(text)
        if (dateMatcher.find()) {
            task.date = dateMatcher.group() // 找到第一个匹配的日期，比如 "2025年12月19日"
        }

        // 3. 提取时间 (正则：匹配 15:00 或 18:00-22:00)
        // \d{1,2}:\d{2} 表示 12:30 这种格式
        val timePattern = Pattern.compile("\\d{1,2}:\\d{2}(-\\d{1,2}:\\d{2})?")
        val timeMatcher = timePattern.matcher(text)
        if (timeMatcher.find()) {
            task.time = timeMatcher.group() // 比如 "15:00"
        }

        // 4. 提取地点 (正则：匹配 "地点" 或 "地址" 或 "位置" 后面的文字)
        // (?<=...) 是“后顾断言”，意思是查找“地点”后面的内容
        // [：:]? 匹配中文或英文冒号，或者没有冒号
        // (.*) 捕获整行内容
        val locationPattern = Pattern.compile("(?<=[地Loc][点址ation])[:：]?\\s*(.*)")
        val locationMatcher = locationPattern.matcher(text)
        if (locationMatcher.find()) {
            // group(1) 是括号里匹配到的内容
            task.location = locationMatcher.group(1)?.trim() ?: ""
        } else {
            // 如果没找到明确的【地点】标签，尝试找包含"楼"、"室"、"厅"的行
            val implicitLoc = lines.find { it.contains("楼") || it.contains("室") || it.contains("厅") }
            if (implicitLoc != null) {
                // 简单的去噪，去掉前面的"地点"两个字（如果有的话）
                task.location = implicitLoc.replace("【地点】", "").replace("地点", "").trim()
            }
        }

        return task
    }
}
```

## 新增相机拍摄对焦/放大缩小以及截取拍摄图片指定区域进行识别的功能

目前这个相机是基于cameraX实现的，仅能拍照，点击拍照按钮会立即拍摄并进行字符识别与提取，不能对待提取图片进行预处理(如裁剪，旋转，放大缩小等)，同时分离识别与提取，目前的识别结果页如下，直接就是识别与提取处理过后的结果，分成两个页面，拍摄完成以及预处理后先进行OCR识别页面，该页面展示原图与识别出来的原始文本(需要可编辑，不是只读文本框)，用户可以编辑修改提出来的原始文本，操作完成之后点击确认，在进行人工智能大语言模型智能提取
![image-20251229220010968](./image-20251229220010968.png)



## 优化用户体验

1. 之前我提到

   > 目前我仅能点击下边栏的四个按钮(中间的相机按钮除外)，如图所示，以切换4个页面，现在我添加通过左右滑动也能切换的功能

​	目前以上功能已经实现，不过现在有个小问题，表现为用户容易误操作，即如果用户尝试向下滑动，但根据人体工学，势必不是完全竖直往下滑动肯定会向左或向右的偏移，目前太灵敏了，很容易导致原本想向下滑动，结果滑到左/右的另外一页了，请进行优化

2. 优化日程页的排版，目前的日程页如下所示，注意图中`今天`按钮左侧还有两个“隐藏的”(底色也为白色而已)按钮分别为`<` `>`，这个排版需要优化，把这两个翻页按钮改到右下并改为两个圆形按钮，左右排列
   ![image-20251229211512442](./image-20251229211512442.png)
   以下是目前的`CalendarFragment.kt`即日程页的实现

   ```kotlin
   package com.example.photodo
   
   import android.os.Bundle
   import android.view.LayoutInflater
   import android.view.View
   import android.view.ViewGroup
   import android.widget.Button
   import android.widget.TextView
   import androidx.fragment.app.Fragment
   import androidx.lifecycle.lifecycleScope
   import androidx.recyclerview.widget.LinearLayoutManager
   import androidx.recyclerview.widget.RecyclerView
   import kotlinx.coroutines.launch
   import java.text.SimpleDateFormat
   import java.util.*
   
   class CalendarFragment : Fragment() {
   
       private lateinit var rvCalendar: RecyclerView
       private lateinit var calendarAdapter: CalendarAdapter
   
       private lateinit var rvTasks: RecyclerView
       private lateinit var taskAdapter: TaskAdapter
   
       private lateinit var tvCurrentMonth: TextView
       private lateinit var btnPrevWeek: Button
       private lateinit var btnNextWeek: Button
       private lateinit var btnBackToday: Button
   
       // 当前选中的日期（默认今天）
       private var selectedDate: Date = Date()
       // 当前显示的周的基准日期（用于计算那一周的7天）
       private var currentWeekBaseDate: Date = Date()
   
       override fun onCreateView(
           inflater: LayoutInflater, container: ViewGroup?,
           savedInstanceState: Bundle?
       ): View? {
           return inflater.inflate(R.layout.fragment_calendar, container, false)
       }
   
       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           super.onViewCreated(view, savedInstanceState)
   
           // 绑定控件
           tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
           btnPrevWeek = view.findViewById(R.id.btnPrevWeek)
           btnNextWeek = view.findViewById(R.id.btnNextWeek)
           btnBackToday = view.findViewById(R.id.btnBackToday)
           rvCalendar = view.findViewById(R.id.rvCalendar)
           rvTasks = view.findViewById(R.id.rvTasks)
   
           // 1. 初始化下面的任务列表
           rvTasks.layoutManager = LinearLayoutManager(context)
           taskAdapter = TaskAdapter(mutableListOf())
           rvTasks.adapter = taskAdapter
   
           // 2. 初始化上面的日历列表
           rvCalendar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
           calendarAdapter = CalendarAdapter(emptyList()) { clickedDate ->
               // 当点击某一天时：
               selectedDate = clickedDate
               refreshTaskList(clickedDate)
           }
           rvCalendar.adapter = calendarAdapter
   
           // 3. 按钮点击事件
           btnPrevWeek.setOnClickListener {
               changeWeek(-7) // 减7天
           }
           btnNextWeek.setOnClickListener {
               changeWeek(7) // 加7天
           }
           btnBackToday.setOnClickListener {
               // 回到今天
               val today = Date()
               selectedDate = today
               currentWeekBaseDate = today
               updateCalendarUI()
           }
   
           // 4. 首次加载
           updateCalendarUI()
       }
   
       /**
        * 核心算法：根据 currentWeekBaseDate 计算出一周7天的数据
        */
       private fun updateCalendarUI() {
           // 1. 计算本周的第一天（周一）
           val calendar = Calendar.getInstance(Locale.CHINA)
           calendar.time = currentWeekBaseDate
   
           // 设置一周的第一天是周一 (中国习惯)
           calendar.firstDayOfWeek = Calendar.MONDAY
           // 调整到本周的周一
           calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
   
           // 2. 循环生成7天的数据
           val weekDays = mutableListOf<CalendarDay>()
           val sdfWeek = SimpleDateFormat("E", Locale.CHINA) // "周一"
           val sdfDay = SimpleDateFormat("d", Locale.CHINA)  // "20"
   
           // 记录一下这周第一天，用来更新顶部的 "2025年12月"
           val firstDayOfWeek = calendar.time
   
           for (i in 0 until 7) {
               val date = calendar.time
               val isSelected = isSameDay(date, selectedDate)
   
               weekDays.add(CalendarDay(
                   date = date,
                   weekStr = sdfWeek.format(date),
                   dayStr = sdfDay.format(date),
                   isSelected = isSelected
               ))
   
               // 天数+1，准备下一次循环
               calendar.add(Calendar.DAY_OF_MONTH, 1)
           }
   
           // 3. 更新日历列表
           calendarAdapter.updateDays(weekDays)
   
           // 4. 更新顶部标题 (显示 年-月)
           val sdfTitle = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
           tvCurrentMonth.text = sdfTitle.format(firstDayOfWeek)
   
           // 5. 刷新下面的任务列表 (查询选中那天的任务)
           refreshTaskList(selectedDate)
       }
   
       private fun changeWeek(amount: Int) {
           val calendar = Calendar.getInstance()
           calendar.time = currentWeekBaseDate
           calendar.add(Calendar.DAY_OF_MONTH, amount)
           currentWeekBaseDate = calendar.time
           updateCalendarUI()
       }
   
       private fun refreshTaskList(date: Date) {
           // --- 核心修改点 ---
           // 把 Date 转成 String，必须使用标准格式 "yyyy-MM-dd" 才能匹配数据库
           val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
           val dateStr = sdfDb.format(date)
           // ------------------
   
           viewLifecycleOwner.lifecycleScope.launch {
               context?.let { ctx ->
                   val db = AppDatabase.getDatabase(ctx)
                   // 此时 dateStr 是 "2025-12-22"，与数据库内容完美匹配
                   val tasks = db.taskDao().getTasksByDate(dateStr)
                   taskAdapter.updateData(tasks)
               }
           }
       }
   
       // 辅助函数：判断两天是不是同一天 (用于UI高亮，不需要改)
       private fun isSameDay(date1: Date, date2: Date): Boolean {
           val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
           return sdf.format(date1) == sdf.format(date2)
       }
   }
   ```

   以下为对应的xml样式文件
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       android:orientation="vertical"
       android:background="#F5F5F5"
       android:fitsSystemWindows="true">
   
       <androidx.cardview.widget.CardView
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:layout_marginBottom="2dp"
           app:cardElevation="2dp"
           app:cardCornerRadius="0dp"
           app:cardBackgroundColor="#FFFFFF">
   
           <LinearLayout
               android:layout_width="match_parent"
               android:layout_height="wrap_content"
               android:orientation="vertical"
               android:paddingTop="40dp"
               android:paddingBottom="12dp">
   
               <LinearLayout
                   android:layout_width="match_parent"
                   android:layout_height="wrap_content"
                   android:gravity="center_vertical"
                   android:paddingStart="16dp"
                   android:paddingEnd="8dp"
                   android:paddingTop="12dp"
                   android:paddingBottom="8dp">
   
                   <TextView
                       android:id="@+id/tvCurrentMonth"
                       android:layout_width="0dp"
                       android:layout_height="wrap_content"
                       android:layout_weight="1"
                       android:text="2025年12月"
                       android:textColor="#333333"
                       android:textSize="20sp"
                       android:textStyle="bold" />
   
                   <Button
                       android:id="@+id/btnPrevWeek"
                       style="@style/Widget.AppCompat.Button.Borderless"
                       android:layout_width="40dp"
                       android:layout_height="40dp"
                       android:text="&lt;"
                       android:textSize="18sp"
                       android:textColor="#757575"/>
   
                   <Button
                       android:id="@+id/btnNextWeek"
                       style="@style/Widget.AppCompat.Button.Borderless"
                       android:layout_width="40dp"
                       android:layout_height="40dp"
                       android:text="&gt;"
                       android:textSize="18sp"
                       android:textColor="#757575"/>
   
                   <Button
                       android:id="@+id/btnBackToday"
                       style="@style/Widget.MaterialComponents.Button.TextButton"
                       android:layout_width="wrap_content"
                       android:layout_height="wrap_content"
                       android:text="今天"
                       android:textColor="?attr/colorPrimary"/>
               </LinearLayout>
   
               <androidx.recyclerview.widget.RecyclerView
                   android:id="@+id/rvCalendar"
                   android:layout_width="match_parent"
                   android:layout_height="wrap_content"
                   android:paddingStart="12dp"
                   android:paddingEnd="12dp"
                   android:clipToPadding="false"/>
           </LinearLayout>
       </androidx.cardview.widget.CardView>
   
       <TextView
           android:layout_width="wrap_content"
           android:layout_height="wrap_content"
           android:text="当日安排"
           android:textSize="14sp"
           android:textColor="#757575"
           android:layout_marginTop="16dp"
           android:layout_marginStart="16dp"
           android:layout_marginBottom="8dp"/>
   
       <androidx.recyclerview.widget.RecyclerView
           android:id="@+id/rvTasks"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:paddingStart="16dp"
           android:paddingEnd="16dp"
           android:clipToPadding="false"
           android:paddingBottom="80dp"/>
   
   </LinearLayout>
   ```

3. 优化显示以及添加日程卡片编辑功能
   目前有一个显示的小问题，即当我在拍照并识别添加了一个日程之后，这时切到日程页或是ToDo页，这个新添加的日程不会立刻同步，而是要再次切换页面并切回才会显示，另外，目前的日程卡片还不具备编辑功能，仅能删除(这个删除也有点毛病，就是仅能在历史记录页面删除，别的页面日程卡片虽然有垃圾桶即删除按钮，但点击没有反应，也需要优化)







