package app.leo.alibi_cam.ui.components.RecorderScreen.atoms

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * 广告 Banner 组件（手动解码图片，零额外依赖）
 *
 * 从远程 URL 下载广告图片，显示在首页上方。
 * 加载失败时静默隐藏，不影响用户体验。
 *
 * @param imageUrl 广告图片 URL
 * @param clickUrl 点击跳转 URL
 * @param onDismiss 用户点击 X 关闭按钮时的回调
 */
@Composable
fun BannerAd(
    imageUrl: String,
    clickUrl: String,
    onDismiss: () -> Unit,
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 异步下载图片
    LaunchedEffect(imageUrl) {
        try {
            val loaded = withContext(Dispatchers.IO) {
                val conn = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                BitmapFactory.decodeStream(conn.inputStream).also {
                    conn.disconnect()
                }
            }
            bitmap = loaded
        } catch (e: Exception) {
            loadFailed = true
        }
    }

    // 加载失败 → 不显示任何内容
    if (loadFailed || (bitmap == null && !loadFailed)) return

    bitmap?.let { bmp ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 广告图片
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // 点击跳转广告链接
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
                        context.startActivity(intent)
                    }
            )

            // 关闭按钮（右上角 X）
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭广告",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
