package app.leo.alibi_cam.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes camera debug info to a plain-text file on external storage
 * so users can inspect it without using Logcat.
 */
object CameraDebugLog {

    private const val TAG = "CameraDebugLog"
    private const val FILE_NAME = "Alibi_camera_debug.txt"

    private var file: File? = null
    private val buffer = StringBuilder()

    @Synchronized
    fun init(context: Context) {
        if (file != null) return
        // Use app-specific external files — no storage permission needed
        file = File(context.getExternalFilesDir(null), FILE_NAME)
        // Clear previous log
        file?.writeText("")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        append("═══════════════════════════════════════")
        append("  Alibi Camera Debug Log")
        append("  $timestamp")
        append("═══════════════════════════════════════")
        append("")
        append("📁 Log file: ${file?.absolutePath}")
        append("")
        Log.i(TAG, "Debug log initialized at ${file?.absolutePath}")
        // Show Toast so user knows where to find the file
        Toast.makeText(context,
            "📁 相机调试日志:\n${file?.absolutePath}",
            Toast.LENGTH_LONG).show()
    }

    @Synchronized
    fun append(line: String) {
        Log.i(TAG, line)
        buffer.append(line).append("\n")
        flush()
    }

    @Synchronized
    fun flush() {
        try {
            file?.appendText(buffer.toString())
            buffer.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write debug log: ${e.message}")
        }
    }

    @Synchronized
    fun getFilePath(): String = file?.absolutePath ?: "(not initialized)"

    /**
     * Show a Toast with the log file path so the user can find it.
     */
    fun showPathToast(context: Context) {
        val path = getFilePath()
        Toast.makeText(
            context,
            "📁 调试日志已保存: $path",
            Toast.LENGTH_LONG
        ).show()
    }
}
