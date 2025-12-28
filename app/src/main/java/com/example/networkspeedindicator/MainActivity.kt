package com.example.networkspeedindicator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var floatWindowToggle: Button
    private lateinit var lockPositionSwitch: Switch
    private lateinit var lowSpeedHideSwitch: Switch
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var textSizeValue: TextView
    private lateinit var showAboveStatusBarSwitch: Switch

    private val REQUEST_OVERLAY_PERMISSION = 1001
    private val REQUEST_NOTIFICATION_PERMISSION = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = Settings(this)
        initViews()
        loadSettings()
        setupListeners()
    }

    private fun initViews() {
        floatWindowToggle = findViewById(R.id.floatWindowToggle)
        lockPositionSwitch = findViewById(R.id.lockPositionSwitch)
        lowSpeedHideSwitch = findViewById(R.id.lowSpeedHideSwitch)
        textSizeSeekBar = findViewById(R.id.textSizeSeekBar)
        textSizeValue = findViewById(R.id.textSizeValue)
        showAboveStatusBarSwitch = findViewById(R.id.showAboveStatusBarSwitch)
    }

    private fun loadSettings() {
        // 加载悬浮窗状态
        updateFloatWindowToggle()
        // 加载位置锁定状态
        lockPositionSwitch.isChecked = settings.isPositionLocked
        // 加载低速隐藏状态
        lowSpeedHideSwitch.isChecked = settings.isLowSpeedHideEnabled
        // 加载网速格式
        when (settings.speedFormat) {
            0 -> findViewById<RadioButton>(R.id.speedFormatTotal).isChecked = true
            1 -> findViewById<RadioButton>(R.id.speedFormatHorizontal).isChecked = true
            2 -> findViewById<RadioButton>(R.id.speedFormatVertical).isChecked = true
        }
        // 加载文字对齐
        when (settings.textAlignment) {
            0 -> findViewById<RadioButton>(R.id.textAlignmentLeft).isChecked = true
            1 -> findViewById<RadioButton>(R.id.textAlignmentCenter).isChecked = true
            2 -> findViewById<RadioButton>(R.id.textAlignmentRight).isChecked = true
        }
        // 加载文字大小
        textSizeSeekBar.progress = settings.textSize
        textSizeValue.text = "${settings.textSize}sp"
        // 加载显示在状态栏上方状态
        showAboveStatusBarSwitch.isChecked = settings.showAboveStatusBar
    }

    private fun setupListeners() {
        // 悬浮窗开关
        floatWindowToggle.setOnClickListener {
            toggleFloatWindow()
        }

        // 位置锁定开关
        lockPositionSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.isPositionLocked = isChecked
            updateFloatWindowSettings()
        }

        // 低速隐藏开关
        lowSpeedHideSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.isLowSpeedHideEnabled = isChecked
            updateFloatWindowSettings()
        }

        // 网速格式单选按钮
        findViewById<RadioButton>(R.id.speedFormatTotal).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.speedFormat = 0
                updateFloatWindowSettings()
            }
        }
        findViewById<RadioButton>(R.id.speedFormatHorizontal).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.speedFormat = 1
                updateFloatWindowSettings()
            }
        }
        findViewById<RadioButton>(R.id.speedFormatVertical).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.speedFormat = 2
                updateFloatWindowSettings()
            }
        }

        // 文字对齐单选按钮
        findViewById<RadioButton>(R.id.textAlignmentLeft).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.textAlignment = 0
                updateFloatWindowSettings()
            }
        }
        findViewById<RadioButton>(R.id.textAlignmentCenter).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.textAlignment = 1
                updateFloatWindowSettings()
            }
        }
        findViewById<RadioButton>(R.id.textAlignmentRight).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.textAlignment = 2
                updateFloatWindowSettings()
            }
        }

        // 文字大小滑块
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSizeValue.text = "$progress sp"
                settings.textSize = progress
                updateFloatWindowSettings()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 显示在状态栏上方开关
        showAboveStatusBarSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.showAboveStatusBar = isChecked
            updateFloatWindowSettings()
        }
    }

    // 更新悬浮窗设置
    private fun updateFloatWindowSettings() {
        if (settings.isFloatWindowEnabled) {
            val intent = Intent(this, FloatWindowService::class.java)
            intent.action = "UPDATE_SETTINGS"
            startService(intent)
        }
    }

    private fun toggleFloatWindow() {
        if (settings.isFloatWindowEnabled) {
            // 关闭悬浮窗
            stopService(Intent(this, FloatWindowService::class.java))
            settings.isFloatWindowEnabled = false
        } else {
            // 检查并请求权限
            if (checkPermissions()) {
                // 开启悬浮窗
                startService(Intent(this, FloatWindowService::class.java))
                settings.isFloatWindowEnabled = true
            }
        }
        updateFloatWindowToggle()
    }

    private fun updateFloatWindowToggle() {
        if (settings.isFloatWindowEnabled) {
            floatWindowToggle.text = getString(R.string.disable_float_window)
        } else {
            floatWindowToggle.text = getString(R.string.enable_float_window)
        }
    }

    private fun checkPermissions(): Boolean {
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
                return false
            }
        }

        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                return false
            }
        }

        return true
    }

    // 颜色选择点击事件
    fun onColorClick(view: android.view.View) {
        val color = when (view.id) {
            R.id.colorBlack -> android.graphics.Color.BLACK
            R.id.colorWhite -> android.graphics.Color.WHITE
            R.id.colorPurple -> resources.getColor(R.color.purple_500, theme)
            R.id.colorTeal -> resources.getColor(R.color.teal_200, theme)
            else -> android.graphics.Color.WHITE
        }
        settings.textColor = color
    }

    // 位置微调按钮点击事件
    fun onMoveButtonClick(view: android.view.View) {
        val delta = 10 // 微调步长
        val intent = Intent(this, FloatWindowService::class.java)
        when (view.id) {
            R.id.moveUpButton -> {
                settings.floatWindowY -= delta
                intent.action = "MOVE_FLOAT_WINDOW"
                intent.putExtra("DELTA_X", 0)
                intent.putExtra("DELTA_Y", -delta)
                startService(intent)
            }
            R.id.moveDownButton -> {
                settings.floatWindowY += delta
                intent.action = "MOVE_FLOAT_WINDOW"
                intent.putExtra("DELTA_X", 0)
                intent.putExtra("DELTA_Y", delta)
                startService(intent)
            }
            R.id.moveLeftButton -> {
                settings.floatWindowX -= delta
                intent.action = "MOVE_FLOAT_WINDOW"
                intent.putExtra("DELTA_X", -delta)
                intent.putExtra("DELTA_Y", 0)
                startService(intent)
            }
            R.id.moveRightButton -> {
                settings.floatWindowX += delta
                intent.action = "MOVE_FLOAT_WINDOW"
                intent.putExtra("DELTA_X", delta)
                intent.putExtra("DELTA_Y", 0)
                startService(intent)
            }
        }
    }

    // 重置位置按钮点击事件
    fun onResetPositionClick(view: android.view.View) {
        settings.resetPosition()
        val intent = Intent(this, FloatWindowService::class.java)
        intent.action = "RESET_FLOAT_WINDOW"
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 权限已授予，开启悬浮窗
                    startService(Intent(this, FloatWindowService::class.java))
                    settings.isFloatWindowEnabled = true
                    updateFloatWindowToggle()
                } else {
                    Toast.makeText(this, "需要悬浮窗权限才能使用此功能", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，开启悬浮窗
                startService(Intent(this, FloatWindowService::class.java))
                settings.isFloatWindowEnabled = true
                updateFloatWindowToggle()
            } else {
                Toast.makeText(this, "需要通知权限才能使用此功能", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
