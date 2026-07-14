package app.leo.alibi_cam.helpers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 更新信息数据类
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val message: String,
    val downloadUrl: String,
)

/**
 * 广告配置数据类（从 update.json 远程拉取）
 * @param enabled 是否启用广告（默认 false）
 * @param version 广告版本号，用户关闭后同一版本不再显示
 * @param imageUrl 广告图片地址（远程 PNG/JPG 等）
 * @param clickUrl 点击广告跳转地址
 */
data class AdConfig(
    val enabled: Boolean,
    val version: Int,
    val imageUrl: String,
    val clickUrl: String,
)

/**
 * 更新检查帮助类
 *
 * 从 Gitee raw 文件拉取 update.json，比对版本号。
 * 用户需要将 update.json 上传到 Gitee 仓库根目录。
 *
 * 替换 UPDATE_URL 中的 {your-username} 为你的 Gitee 用户名。
 */
object UpdateHelper {
    private const val TAG = "UpdateHelper"

    // ── Gitee 仓库 raw 文件地址 ──
    private const val UPDATE_URL =
        "https://gitee.com/zhanglinleo1-maker/Alibi-Cam/raw/master/update.json"

    /**
     * 从 Gitee 拉取 update.json，解析广告配置。
     * 如果 JSON 中没有 ad 字段或解析失败，返回默认关闭的 AdConfig。
     */
    suspend fun fetchAdConfig(): AdConfig {
        return withContext(Dispatchers.IO) {
            try {
                val conn = (URL(UPDATE_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val json = JSONObject(body)
                val adJson = json.optJSONObject("ad") ?: return@withContext AdConfig(
                    enabled = false, version = 0, imageUrl = "", clickUrl = ""
                )

                AdConfig(
                    enabled = adJson.optBoolean("enabled", false),
                    version = adJson.optInt("version", 0),
                    imageUrl = adJson.optString("imageUrl", ""),
                    clickUrl = adJson.optString("clickUrl", ""),
                )
            } catch (e: Exception) {
                Log.e(TAG, "拉取广告配置失败", e)
                AdConfig(enabled = false, version = 0, imageUrl = "", clickUrl = "")
            }
        }
    }

    /**
     * 从 Gitee 拉取 update.json，如果远端版本 > 当前版本则返回 UpdateInfo，
     * 否则返回 null（无更新或网络错误）。
     */
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val conn = (URL(UPDATE_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val json = JSONObject(body)
                val remoteVersionCode = json.getInt("versionCode")

                if (remoteVersionCode > currentVersionCode) {
                    UpdateInfo(
                        versionCode = remoteVersionCode,
                        versionName = json.getString("versionName"),
                        message = json.getString("message"),
                        downloadUrl = json.getString("downloadUrl"),
                    )
                } else {
                    Log.i(TAG, "已是最新版本 (current=$currentVersionCode, remote=$remoteVersionCode)")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查更新失败", e)
                null
            }
        }
    }
}
