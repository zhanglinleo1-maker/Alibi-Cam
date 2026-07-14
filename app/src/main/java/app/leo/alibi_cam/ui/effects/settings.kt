package app.leo.alibi_cam.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import app.leo.alibi_cam.dataStore
import app.leo.alibi_cam.db.AppSettings

@Composable
fun rememberSettings(): AppSettings {
    return LocalContext.current.dataStore.data.collectAsState(initial = AppSettings.getDefaultInstance()).value
}
