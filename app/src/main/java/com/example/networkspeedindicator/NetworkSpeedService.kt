package com.example.networkspeedindicator

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetworkSpeedService : Service() {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var downloadSpeed: Long = 0
    private var uploadSpeed: Long = 0

    // 回调接口
    interface NetworkSpeedListener {
        fun onNetworkSpeedUpdate(downloadSpeed: Long, uploadSpeed: Long)
    }

    companion object {
        private var listener: NetworkSpeedListener? = null
        private var isRunning = false

        fun setListener(l: NetworkSpeedListener?) {
            listener = l
        }

        fun isServiceRunning(): Boolean {
            return isRunning
        }
    }

    private val updateSpeedRunnable = object : Runnable {
        override fun run() {
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()

            if (lastRxBytes > 0) {
                downloadSpeed = (currentRxBytes - lastRxBytes) / 1000 // KB/s
            }
            if (lastTxBytes > 0) {
                uploadSpeed = (currentTxBytes - lastTxBytes) / 1000 // KB/s
            }

            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes

            // 通知监听器
            listener?.onNetworkSpeedUpdate(downloadSpeed, uploadSpeed)

            // 每秒更新一次
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        // 初始化流量数据
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        // 开始更新网速
        handler.post(updateSpeedRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(updateSpeedRunnable)
        listener = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // 获取格式化的网速字符串
    fun formatSpeed(speed: Long): String {
        return when {
            speed < 1024 -> "$speed KB/s"
            else -> String.format("%.1f MB/s", speed / 1024.0)
        }
    }
}
