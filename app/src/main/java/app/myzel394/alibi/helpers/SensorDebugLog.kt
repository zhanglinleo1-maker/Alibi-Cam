package app.myzel394.alibi.helpers

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 传感器调试日志：将姿态传感器数据持久化到 CSV 文件，便于离线分析误触发原因。
 *
 * 文件位置：{externalFilesDir}/Alibi_sensor_debug.csv
 * 采样率：~5 Hz（跟随 SENSOR_DELAY_NORMAL）
 * 写入策略：每 100 个事件批量刷盘，关键事件立即刷盘
 */
object SensorDebugLog {

    private const val TAG = "SensorDebugLog"
    private const val FILE_NAME = "Alibi_sensor_debug.csv"
    private const val FLUSH_INTERVAL = 100  // 每 100 条刷一次盘

    private var file: File? = null
    private val buffer = StringBuilder()
    private var eventCount = 0
    private var initialized = false

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        file = File(context.getExternalFilesDir(null), FILE_NAME)
        // 覆盖旧日志
        file?.writeText("")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        // CSV 表头
        buffer.append("# Alibi 传感器调试日志 — $timestamp\n")
        buffer.append("# 列: eventTimeMs, rawAngle, smoothedAngle, threshold, ")
        buffer.append("isDeviating, deviationElapsedMs, stableElapsedMs, ")
        buffer.append("refW, refX, refY, refZ, ")
        buffer.append("curW, curX, curY, curZ\n")
        flush()
        initialized = true
        Log.i(TAG, "传感器调试日志已初始化: ${file?.absolutePath}")
    }

    /**
     * 记录传感器采样数据（高频调用，仅内存缓冲，定期刷盘）
     */
    @Synchronized
    fun logSensorEvent(
        rawAngle: Float,
        smoothedAngle: Float,
        threshold: Float,
        isDeviating: Boolean,
        deviationElapsedMs: Long,
        stableElapsedMs: Long,
        refQ: FloatArray?,
        curQ: FloatArray?,
    ) {
        if (!initialized) return

        val now = System.currentTimeMillis()
        val refW = refQ?.getOrNull(3) ?: Float.NaN
        val refX = refQ?.getOrNull(0) ?: Float.NaN
        val refY = refQ?.getOrNull(1) ?: Float.NaN
        val refZ = refQ?.getOrNull(2) ?: Float.NaN
        val curW = curQ?.getOrNull(3) ?: Float.NaN
        val curX = curQ?.getOrNull(0) ?: Float.NaN
        val curY = curQ?.getOrNull(1) ?: Float.NaN
        val curZ = curQ?.getOrNull(2) ?: Float.NaN

        buffer.append("$now,${"%.2f".format(rawAngle)},${"%.2f".format(smoothedAngle)},")
        buffer.append("${"%.1f".format(threshold)},$isDeviating,$deviationElapsedMs,$stableElapsedMs,")
        buffer.append("${"%.6f".format(refW)},${"%.6f".format(refX)},${"%.6f".format(refY)},${"%.6f".format(refZ)},")
        buffer.append("${"%.6f".format(curW)},${"%.6f".format(curX)},${"%.6f".format(curY)},${"%.6f".format(curZ)}\n")

        eventCount++
        if (eventCount >= FLUSH_INTERVAL) {
            flush()
        }
    }

    /**
     * 记录关键事件（参考捕获、偏离开始/结束、警告触发、自动停止等）
     */
    @Synchronized
    fun logEvent(event: String) {
        if (!initialized) return
        val now = System.currentTimeMillis()
        buffer.append("# [$now] *** EVENT: $event ***\n")
        flush()  // 关键事件立即刷盘
        Log.i(TAG, "📐 $event")
    }

    @Synchronized
    fun flush() {
        try {
            file?.appendText(buffer.toString())
            buffer.clear()
            eventCount = 0
        } catch (e: Exception) {
            Log.e(TAG, "写入传感器日志失败: ${e.message}")
        }
    }

    @Synchronized
    fun getFilePath(): String = file?.absolutePath ?: "(未初始化)"

    @Synchronized
    fun close() {
        flush()
        initialized = false
        file = null
        Log.i(TAG, "传感器调试日志已关闭")
    }
}
