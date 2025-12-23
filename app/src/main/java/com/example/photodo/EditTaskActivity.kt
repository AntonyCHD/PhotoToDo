package com.example.photodo // 确认包名正确，无 ui 子包

import android.app.DatePickerDialog // <--- 新增导入
import android.app.TimePickerDialog // <--- 新增导入
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
//import com.example.photodo.db.AppDatabase // 确保导入数据库
//import com.example.photodo.db.Task        // 确保导入实体类
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Calendar // <--- 新增导入：用于获取当前时间

class EditTaskActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var btnSave: Button

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

        // 3. 设置日期和时间选择器 (核心新增功能)
        setupPickers() // <--- 调用新增的方法

        // 4. 接收 MainActivity 传过来的图片 URI
        val uriString = intent.getStringExtra("image_uri")
        if (uriString != null) {
            val uri = Uri.parse(uriString)
            ivPreview.setImageURI(uri)
            runOCR(uri)
        }

        // 5. 保存按钮逻辑
        btnSave.setOnClickListener {
            saveData()
        }
    }

    // --- 新增方法：配置日期和时间弹窗 ---
    private fun setupPickers() {
        // [日期选择器]
        etDate.setOnClickListener {
            // 获取当前日期作为弹窗的默认值
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // 弹出系统原生日期选择框
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 格式化为 YYYY-MM-DD (注意月份要+1)
                    val dateString = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    etDate.setText(dateString)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // [时间选择器]
        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // 弹出系统原生时间选择框
            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    // 格式化为 HH:MM (例如 08:05)
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    etTime.setText(timeString)
                },
                hour, minute, true // true 表示使用24小时制，false为上午/下午
            )
            timePickerDialog.show()
        }
    }

//    private fun runOCR(uri: Uri) {
//        Toast.makeText(this, "正在智能识别中...", Toast.LENGTH_SHORT).show()
//        try {
//            // 使用 ImageUtils 处理图片
//            val bitmap = ImageUtils.getBitmapFromUri(this, uri)
//            // 简单的判空保护
//            if (bitmap == null) {
//                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            val finalBitmap = ImageUtils.ensureMinSize(bitmap)
//
//            val image = InputImage.fromBitmap(finalBitmap, 0)
//            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
//
//            recognizer.process(image)
//                .addOnSuccessListener { visionText ->
//                    // 识别成功，调用 SmartParser 解析
//                    val taskInfo = SmartParser.parseTextToTask(visionText.text)
//
//                    // 自动填入输入框
//                    etTitle.setText(taskInfo.title)
//                    etDate.setText(taskInfo.date)
//                    etTime.setText(taskInfo.time)
//                    etLocation.setText(taskInfo.location)
//
//                    Toast.makeText(this, "识别完成，请核对", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "识别失败: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
    private fun runOCR(uri: Uri) {
        Toast.makeText(this, "正在智能识别中...", Toast.LENGTH_SHORT).show()
        try {
            val bitmap = ImageUtils.getBitmapFromUri(this, uri)
            if (bitmap == null) return

            val finalBitmap = ImageUtils.ensureMinSize(bitmap)
            val image = InputImage.fromBitmap(finalBitmap, 0)
            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // 1. 获取原始 OCR 解析结果
                    val taskInfo = SmartParser.parseTextToTask(visionText.text)

                    // 2. 自动填入标题和地点 (直接填)
                    etTitle.setText(taskInfo.title)
                    etLocation.setText(taskInfo.location)

                    // 3. 【核心修改】填入日期前，先进行标准化清洗！
                    // 无论 OCR 识别出 "2025年..." 还是 "2025/..."，这里都会变成 "2025-MM-DD"
                    val standardDate = normalizeDate(taskInfo.date)
                    etDate.setText(standardDate)

                    // 4. 【核心修改】填入时间前，也标准化
                    val standardTime = normalizeTime(taskInfo.time)
                    etTime.setText(standardTime)

                    Toast.makeText(this, "识别完成 (格式已自动校正)", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "识别失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun saveData() {
        val titleInput = etTitle.text.toString()
        val dateInput = etDate.text.toString()
        val timeInput = etTime.text.toString()

        // 简单校验
        if (titleInput.isBlank()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }

        val newTask = Task(
            title = titleInput,
            date = dateInput,
            time = timeInput,
            location = etLocation.text.toString()
        )

        lifecycleScope.launch {
            // 注意：这里确保引用正确的 AppDatabase 路径
            AppDatabase.getDatabase(this@EditTaskActivity).taskDao().insert(newTask)
            Toast.makeText(this@EditTaskActivity, "✅ 保存成功", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    // --- 工具方法：强制标准化日期格式 (YYYY-MM-DD) ---
    private fun normalizeDate(rawDate: String): String {
        try {
            // 正则逻辑：寻找 4位数字 + 任意分隔符 + 1~2位数字 + 任意分隔符 + 1~2位数字
            // 例子：2025年12月1日 -> 提取出 2025, 12, 1
            val regex = Regex("(\\d{4})\\D+(\\d{1,2})\\D+(\\d{1,2})")
            val matchResult = regex.find(rawDate)

            if (matchResult != null) {
                val (year, month, day) = matchResult.destructured
                // 格式化：%02d 表示不足两位补0 (1 -> 01)
                return String.format("%s-%02d-%02d", year, month.toInt(), day.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rawDate // 如果解析失败，就保持原样
    }

    // --- 工具方法：强制标准化时间格式 (HH:MM) ---
    private fun normalizeTime(rawTime: String): String {
        try {
            // 正则逻辑：寻找 1~2位数字 + 任意分隔符 + 1~2位数字
            // 例子：12:5 -> 提取出 12, 5
            val regex = Regex("(\\d{1,2})\\D+(\\d{1,2})")
            val matchResult = regex.find(rawTime)

            if (matchResult != null) {
                val (hour, minute) = matchResult.destructured
                return String.format("%02d:%02d", hour.toInt(), minute.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rawTime
    }
}