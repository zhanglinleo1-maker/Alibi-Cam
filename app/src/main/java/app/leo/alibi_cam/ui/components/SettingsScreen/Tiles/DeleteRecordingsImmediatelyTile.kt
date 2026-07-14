package app.leo.alibi_cam.ui.components.SettingsScreen.Tiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.leo.alibi_cam.R
import app.leo.alibi_cam.dataStore
import app.leo.alibi_cam.db.AppSettings
import app.leo.alibi_cam.ui.components.atoms.SettingsTile
import kotlinx.coroutines.launch

@Composable
fun DeleteRecordingsImmediatelyTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()

    val dataStore = LocalContext.current.dataStore

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_deleteRecordingsImmediately_title),
        description = stringResource(R.string.ui_settings_option_deleteRecordingsImmediately_description),
        leading = {
            Icon(
                Icons.Default.DeleteSweep,
                contentDescription = null,
            )
        },
        trailing = {
            Switch(
                checked = settings.deleteRecordingsImmediately,
                onCheckedChange = {
                    scope.launch {
                        dataStore.updateData {
                            it.setDeleteRecordingsImmediately(it.deleteRecordingsImmediately.not())
                        }
                    }
                }
            )
        }
    )
}