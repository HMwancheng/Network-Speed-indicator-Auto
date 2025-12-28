package com.example.networkspeedindicator

import android.content.Context
import android.content.SharedPreferences

class Settings(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("network_speed_indicator", Context.MODE_PRIVATE)

    // 悬浮窗是否启用
    var isFloatWindowEnabled: Boolean
        get() = sharedPreferences.getBoolean("is_float_window_enabled", false)
        set(value) = sharedPreferences.edit().putBoolean("is_float_window_enabled", value).apply()

    // 位置是否锁定
    var isPositionLocked: Boolean
        get() = sharedPreferences.getBoolean("is_position_locked", false)
        set(value) = sharedPreferences.edit().putBoolean("is_position_locked", value).apply()

    // 低速隐藏
    var isLowSpeedHideEnabled: Boolean
        get() = sharedPreferences.getBoolean("is_low_speed_hide_enabled", false)
        set(value) = sharedPreferences.edit().putBoolean("is_low_speed_hide_enabled", value).apply()

    // 悬浮窗X坐标
    var floatWindowX: Int
        get() = sharedPreferences.getInt("float_window_x", 100)
        set(value) = sharedPreferences.edit().putInt("float_window_x", value).apply()

    // 悬浮窗Y坐标
    var floatWindowY: Int
        get() = sharedPreferences.getInt("float_window_y", 100)
        set(value) = sharedPreferences.edit().putInt("float_window_y", value).apply()

    // 网速格式：0-总 @网速, 1-上下行横排, 2-上下行竖排
    var speedFormat: Int
        get() = sharedPreferences.getInt("speed_format", 0)
        set(value) = sharedPreferences.edit().putInt("speed_format", value).apply()

    // 文字对齐：0-左, 1-中, 2-右
    var textAlignment: Int
        get() = sharedPreferences.getInt("text_alignment", 1)
        set(value) = sharedPreferences.edit().putInt("text_alignment", value).apply()

    // 文字颜色
    var textColor: Int
        get() = sharedPreferences.getInt("text_color", android.graphics.Color.WHITE)
        set(value) = sharedPreferences.edit().putInt("text_color", value).apply()

    // 文字大小（sp）
    var textSize: Int
        get() = sharedPreferences.getInt("text_size", 12)
        set(value) = sharedPreferences.edit().putInt("text_size", value).apply()

    // 是否显示在状态栏上方
    var showAboveStatusBar: Boolean
        get() = sharedPreferences.getBoolean("show_above_status_bar", false)
        set(value) = sharedPreferences.edit().putBoolean("show_above_status_bar", value).apply()

    // 重置位置
    fun resetPosition() {
        floatWindowX = 100
        floatWindowY = 100
    }
}
