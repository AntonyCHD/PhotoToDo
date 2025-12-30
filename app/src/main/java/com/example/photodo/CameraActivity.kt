package com.example.photodo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnGallery: ImageView
    private lateinit var btnClose: ImageView

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null // ✨ 新增：用于控制对焦和缩放
    private lateinit var cameraExecutor: ExecutorService

    // 注册相册选择器
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { goToEditPage(it) }
    }

    // 注册权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        btnCapture = findViewById(R.id.btnCapture)
        btnGallery = findViewById(R.id.btnGallery)
        btnClose = findViewById(R.id.btnClose)

        // 1. 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // 2. 按钮事件
        btnCapture.setOnClickListener { takePhoto() }
        btnGallery.setOnClickListener { selectImageLauncher.launch("image/*") }
        btnClose.setOnClickListener { finish() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("ClickableViewAccessibility") // 忽略触摸无障碍警告
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                // ✨ 核心修改：绑定生命周期时，获取 camera 实例
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // ✨ 启动手势监听（对焦 & 缩放）
                setupCameraGestures()

            } catch (exc: Exception) {
                Log.e("CameraActivity", "相机启动失败", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // ✨ 新增：配置手势监听
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCameraGestures() {
        // 1. 双指缩放监听器
        val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor // 缩放因子
                // 计算新倍率
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })

        // 2. 单击对焦监听器
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val factory = viewFinder.meteringPointFactory
                val point = factory.createPoint(e.x, e.y)

                // 创建对焦动作：对焦(AF) + 曝光(AE) + 白平衡(AWB)
                // 3秒后如果没有锁定，自动取消对焦状态
//                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF_AE_AWB)
//                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
//                    .build()
                // 修复点：使用位运算组合这三个标志，或者直接用 Builder(point) 默认也是全开
                val action = FocusMeteringAction.Builder(point,
                    FocusMeteringAction.FLAG_AF or
                            FocusMeteringAction.FLAG_AE or
                            FocusMeteringAction.FLAG_AWB
                )
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                camera?.cameraControl?.startFocusAndMetering(action)

                // 可选：在这里加一个简单的 Log 或者 Toast 提示用户已对焦
                // Log.d("Camera", "Focusing at ${e.x}, ${e.y}")
                return true
            }
        })

        // 3. 将触摸事件分发给上面两个监听器
        viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true // 消费事件
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
            .format(System.currentTimeMillis())
        val photoFile = File(externalCacheDir, "PhotoDo_$name.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "拍照失败: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    goToEditPage(savedUri)
                }
            }
        )
    }

    private fun goToEditPage(uri: Uri) {
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra("image_uri", uri.toString())
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}