package app.myzel394.alibi.helpers

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 遥测帮助类：首次安装时通过飞书机器人通知开发者
 *
 * 使用前需要：
 * 1. 打开飞书 → 创建群聊（独自一人即可）
 * 2. 群设置 → 群机器人 → 添加机器人 → 自定义机器人
 * 3. 复制 Webhook URL，把 Hook ID 填入下方常量
 * 4. （可选）安全设置 → 暂不设置签名校验
 *
 * Webhook URL 格式：https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxxxx
 */
object TelemetryHelper {
    private const val TAG = "TelemetryHelper"

    // ── 配置区（替换为你的飞书机器人 Hook ID）──
    // 从 Webhook URL 最后一段取，例如 https://open.feishu.cn/open-apis/bot/v2/hook/abc123-def456
    private const val FEISHU_HOOK_ID = "19995e2c-b85c-4083-8fd2-1a753ca7b13b"

    private const val FEISHU_WEBHOOK = "https://open.feishu.cn/open-apis/bot/v2/hook/$FEISHU_HOOK_ID"

    /**
     * 新设备首次安装时调用。通过飞书机器人发送卡片消息。
     * 如果 Hook ID 未配置，静默跳过。
     */
    suspend fun reportNewInstall(appVersion: String, installId: String) {
        if (FEISHU_HOOK_ID.startsWith("YOUR_")) {
            Log.i(TAG, "飞书 Hook 未配置，跳过遥测上报")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val deviceInfo = "${Build.BRAND} ${Build.MODEL}｜Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

                val markdown = buildString {
                    appendLine("**设备：**$deviceInfo")
                    appendLine("**版本：**v$appVersion")
                    appendLine("**ID：**`$installId`")
                    appendLine("**时间：**$now")
                }

                // 飞书卡片消息
                val json = JSONObject().apply {
                    put("msg_type", "interactive")
                    put("card", JSONObject().apply {
                        put("header", JSONObject().apply {
                            put("title", JSONObject().apply {
                                put("tag", "plain_text")
                                put("content", "📱 Alibi Cam — 新安装")
                            })
                            put("template", "red")
                        })
                        put("elements", JSONArray().apply {
                            put(JSONObject().apply {
                                put("tag", "markdown")
                                put("content", markdown)
                            })
                        })
                    })
                }

                val conn = (URL(FEISHU_WEBHOOK).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                conn.outputStream.use { it.write(json.toString().toByteArray()) }
                Log.i(TAG, "飞书上报完成: HTTP ${conn.responseCode}")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "飞书上报失败", e)
            }
        }
    }
}
