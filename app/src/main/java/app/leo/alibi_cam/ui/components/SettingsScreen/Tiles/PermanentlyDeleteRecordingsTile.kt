package app.leo.alibi_cam.ui.components.SettingsScreen.Tiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.leo.alibi_cam.R
import app.leo.alibi_cam.dataStore
import app.leo.alibi_cam.db.AppSettings
import app.leo.alibi_cam.ui.components.atoms.SettingsTile
import kotlinx.coroutines.launch

/**
 * 直接删除视频文件开关 Tile
 *
 * 关闭时（默认）：自动删除的视频进入系统回收站，用户需定期手动清理。
 * 开启后：视频直接永久删除，立即释放存储空间。
 */
@Composable
fun PermanentlyDeleteRecordingsTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_permanentlyDeleteRecordings_title),
        description = stringResource(R.string.ui_settings_option_permanentlyDeleteRecordings_description),
        leading = {
            Icon(
                Icons.Default.AutoDelete,
                contentDescription = null,
            )
        },
        trailing = {
            Switch(
                checked = settings.permanentlyDeleteRecordings,
                onCheckedChange = {
                    scope.launch {
                        dataStore.updateData {
                            it.setPermanentlyDeleteRecordings(it.permanentlyDeleteRecordings.not())
                        }
                    }
                }
            )
        }
    )
}
