package com.example.photodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.photodo.api.AiClient
import com.example.photodo.api.ChatRequest
import com.example.photodo.api.Message
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import org.json.JSONObject //用于解析AI返回的JSON

class EditTaskActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSave: Button

    // 暂存识别到的原始文本，用于发给 AI
    private var rawRecognizedText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // 1. 初始化 Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarEdit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // 2. 绑定控件
        ivPreview = findViewById(R.id.ivPreview)
        etTitle = findViewById(R.id.etTitle)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        btnSave = findViewById(R.id.btnSave)

        setupPickers()

        // 3. 判断模式
        val uriString = intent.getStringExtra("image_uri")
        if (uriString != null) {
            supportActionBar?.title = "识别结果核对"
            val uri = Uri.parse(uriString)
            ivPreview.setImageURI(uri)
            runOCR(uri)
        } else {
            supportActionBar?.title = "手动新建日程"
            ivPreview.visibility = View.GONE // 手动模式隐藏图片
            // 手动模式下，也可以让用户粘贴一段文字，然后点 AI 分析
            etTitle.hint = "在此输入或粘贴文本，点击右上角【魔法棒】进行AI分析..."
        }

        btnSave.setOnClickListener { saveData() }
    }

    // --- 新增：在右上角添加 AI 魔法棒按钮 ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 101, 0, "AI分析")
            ?.setIcon(android.R.drawable.ic_menu_search) // 暂时用搜索图标，你可以换成星星图标
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 101) {
            // 点击了 AI 分析
            val textToAnalyze = if (rawRecognizedText.isNotEmpty()) {
                rawRecognizedText // 优先用 OCR 识别到的
            } else {
                etTitle.text.toString() // 如果没有 OCR 结果，就分析用户输入的标题栏内容
            }

            if (textToAnalyze.isBlank()) {
                Toast.makeText(this, "没有可分析的文本", Toast.LENGTH_SHORT).show()
            } else {
                analyzeTextWithAi(textToAnalyze)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // --- 核心：调用 AI ---
    private fun analyzeTextWithAi(text: String) {
        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("AI 正在思考中...\n(Qwen3-8B)")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        lifecycleScope.launch {
            try {
                // 1. 获取当前日期和星期，告诉 AI
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd EEEE", java.util.Locale.CHINA)
                val todayStr = sdf.format(java.util.Date())
                // 2. 构造 Prompt (提示词) -- 加了日期上下文
                // 这是一个非常关键的技巧：要求 AI 只返回 JSON，不要废话
                val prompt = """
                    背景信息：
                    今天是：$todayStr
                    
                    请分析以下文本，提取日程信息。
                    文本内容：
                    $text
                    
                    要求：
                    1. 提取“标题”、“日期”、“时间”、“地点”。
                    2. 根据“今天是 $todayStr”推算具体的日期（如“明天”、“下周三”）。
                    3. 日期格式必须统一为 "YYYY-MM-DD"。
                    4. 结果必须以纯 JSON 格式返回，不要包含 Markdown 代码块。
                    5. JSON 格式如下：
                    {"title": "...", "date": "...", "time": "...", "location": "..."}
                """.trimIndent()

                // 2. 发起网络请求 (切换到 IO 线程)
                val response = withContext(Dispatchers.IO) {
                    AiClient.service.getChatCompletion(
                        auth = AiClient.API_KEY,
                        request = ChatRequest(
                            messages = listOf(Message("user", prompt))
                        )
                    )
                }

                // 3. 解析结果
                val aiContent = response.choices.firstOrNull()?.message?.content ?: ""
                // 清理一下可能的 Markdown 符号 (```json ... ```)
                val cleanJson = aiContent.replace("```json", "").replace("```", "").trim()

                if (cleanJson.isNotEmpty()) {
                    val json = JSONObject(cleanJson)
                    etTitle.setText(json.optString("title"))
                    etDate.setText(json.optString("date"))
                    etTime.setText(json.optString("time"))
                    etLocation.setText(json.optString("location"))

                    Toast.makeText(this@EditTaskActivity, "AI 分析完成！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EditTaskActivity, "AI 返回为空", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@EditTaskActivity, "AI 分析失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    // --- 原有逻辑保持不变 ---
    private fun runOCR(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // 1. 保存原始文本
                    rawRecognizedText = visionText.text

                    // 2. 先把原始文本填进去 (可选，或者填到 SmartParser 的结果)
                    // 这里我们先填原始的，让用户可以自己决定要不要点 AI 优化
                    etTitle.setText(rawRecognizedText)

                    // 3. 自动触发一次本地正则解析 (作为兜底)
                    val smartInfo = SmartParser.parseTextToTask(rawRecognizedText)
                    if (smartInfo.title != null) etTitle.setText(smartInfo.title)
                    if (smartInfo.date != null) etDate.setText(smartInfo.date)
                    if (smartInfo.time != null) etTime.setText(smartInfo.time)
                    if (smartInfo.location != null) etLocation.setText(smartInfo.location)

                    Toast.makeText(this, "本地识别完成，点击右上角可使用 AI 深度分析", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "识别失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupPickers() {
        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val month = m + 1
                val dateStr = String.format("%d-%02d-%02d", y, month, d)
                etDate.setText(dateStr)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                val timeStr = String.format("%02d:%02d", h, m)
                etTime.setText(timeStr)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
    }

    private fun saveData() {
        val title = etTitle.text.toString()
        if (title.isBlank()) {
            etTitle.error = "请输入内容"
            return
        }
        val date = etDate.text.toString()
        val time = etTime.text.toString()
        val loc = etLocation.text.toString()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val newTask = Task(title = title, date = date, time = time, location = loc)
            db.taskDao().insert(newTask)
            Toast.makeText(this@EditTaskActivity, "保存成功", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}