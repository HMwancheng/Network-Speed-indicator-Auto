package com.example.networkspeedindicator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatWindowService : Service(), NetworkSpeedService.NetworkSpeedListener {

    private lateinit var windowManager: WindowManager
    private lateinit var floatView: View
    private lateinit var networkSpeedText: TextView
    private lateinit var settings: Settings

    private var params: WindowManager.LayoutParams? = null
    private var isMoving = false
    private var lastX = 0f
    private var lastY = 0f
    private var downloadSpeed = 0L
    private var uploadSpeed = 0L

    private val CHANNEL_ID = "float_window_channel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        settings = Settings(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        initFloatWindow()
        // 启动网络速度服务
        startService(Intent(this, NetworkSpeedService::class.java))
        NetworkSpeedService.setListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除悬浮窗
        if (::windowManager.isInitialized && ::floatView.isInitialized) {
            windowManager.removeView(floatView)
        }
        NetworkSpeedService.setListener(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "MOVE_FLOAT_WINDOW" -> {
                    val deltaX = it.getIntExtra("DELTA_X", 0)
                    val deltaY = it.getIntExtra("DELTA_Y", 0)
                    moveFloatWindow(deltaX, deltaY)
                }
                "RESET_FLOAT_WINDOW" -> {
                    resetPosition()
                }
                "UPDATE_SETTINGS" -> {
                    updateSettings()
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Float Window Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Network Speed Indicator")
            .setContentText("Running")
            .setPriority(Notification.PRIORITY_LOW)
            .build()
    }

    private fun initFloatWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null)
        networkSpeedText = floatView.findViewById(R.id.networkSpeedText)

        // 设置WindowManager参数
        params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (settings.showAboveStatusBar) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.START or Gravity.TOP
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = settings.floatWindowX
            y = settings.floatWindowY
        }

        // 设置悬浮窗触摸事件
        floatView.setOnTouchListener { v, event ->
            if (settings.isPositionLocked) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMoving = true
                    lastX = event.rawX
                    lastY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isMoving) {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        params?.let {
                            it.x += deltaX.toInt()
                            it.y += deltaY.toInt()
                            windowManager.updateViewLayout(floatView, it)
                            settings.floatWindowX = it.x
                            settings.floatWindowY = it.y
                        }
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isMoving = false
                }
            }
            true
        }

        // 应用初始设置
        updateFloatWindowSettings()
        // 添加悬浮窗到窗口管理器
        windowManager.addView(floatView, params)
    }

    // 更新悬浮窗设置
    private fun updateFloatWindowSettings() {
        // 更新文字颜色
        networkSpeedText.setTextColor(settings.textColor)
        // 更新文字大小
        networkSpeedText.textSize = settings.textSize.toFloat()
        // 更新文字对齐方式
        networkSpeedText.gravity = when (settings.textAlignment) {
            0 -> Gravity.LEFT
            1 -> Gravity.CENTER
            2 -> Gravity.RIGHT
            else -> Gravity.CENTER
        }
        // 更新位置
        params?.let {
            it.x = settings.floatWindowX
            it.y = settings.floatWindowY
            windowManager.updateViewLayout(floatView, it)
        }
    }

    // 更新网速显示
    private fun updateNetworkSpeed() {
        // 低速隐藏功能
        if (settings.isLowSpeedHideEnabled && downloadSpeed < 1 && uploadSpeed < 1) {
            floatView.visibility = View.GONE
            return
        } else {
            floatView.visibility = View.VISIBLE
        }

        val speedText = when (settings.speedFormat) {
            0 -> {
                // 总 @网速
                val totalSpeed = downloadSpeed + uploadSpeed
                formatSpeed(totalSpeed)
            }
            1 -> {
                // 上下行横排
                "↓${formatSpeed(downloadSpeed)} ↑${formatSpeed(uploadSpeed)}"
            }
            2 -> {
                // 上下行竖排
                "↓${formatSpeed(downloadSpeed)}\n↑${formatSpeed(uploadSpeed)}"
            }
            else -> {
                val totalSpeed = downloadSpeed + uploadSpeed
                formatSpeed(totalSpeed)
            }
        }
        networkSpeedText.text = speedText
    }

    // 格式化网速
    private fun formatSpeed(speed: Long): String {
        return when {
            speed < 1024 -> "$speed KB/s"
            else -> String.format("%.1f MB/s", speed / 1024.0)
        }
    }

    // 网络速度更新回调
    override fun onNetworkSpeedUpdate(download: Long, upload: Long) {
        downloadSpeed = download
        uploadSpeed = upload
        updateNetworkSpeed()
    }

    // 位置微调方法
    fun moveFloatWindow(deltaX: Int, deltaY: Int) {
        params?.let {
            it.x += deltaX
            it.y += deltaY
            windowManager.updateViewLayout(floatView, it)
            settings.floatWindowX = it.x
            settings.floatWindowY = it.y
        }
    }

    // 重置位置
    fun resetPosition() {
        settings.resetPosition()
        params?.let {
            it.x = settings.floatWindowX
            it.y = settings.floatWindowY
            windowManager.updateViewLayout(floatView, it)
        }
    }

    // 更新设置
    fun updateSettings() {
        updateFloatWindowSettings()
        updateNetworkSpeed()
    }
}
