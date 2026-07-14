package app.leo.alibi_cam.ui.components.SettingsScreen.Tiles

import android.os.Message
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.magnifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.leo.alibi_cam.R
import app.leo.alibi_cam.dataStore
import app.leo.alibi_cam.db.AppLockSettings
import app.leo.alibi_cam.db.AppSettings
import app.leo.alibi_cam.helpers.AppLockHelper
import app.leo.alibi_cam.ui.components.atoms.MessageBox
import app.leo.alibi_cam.ui.components.atoms.MessageType
import app.leo.alibi_cam.ui.components.atoms.SettingsTile
import app.leo.alibi_cam.ui.components.atoms.VisualDensity
import kotlinx.coroutines.launch

@Composable
fun EnableAppLockTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val dataStore = context.dataStore

    val appLockSupport = AppLockHelper.getSupportType(context)

    if (appLockSupport === AppLockHelper.SupportType.UNAVAILABLE) {
        return
    }

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_enableAppLock_title),
        description = stringResource(R.string.ui_settings_option_enableAppLock_description),
        tertiaryLine = {
            if (appLockSupport === AppLockHelper.SupportType.NONE_ENROLLED) {
                Box(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    MessageBox(
                        type = MessageType.WARNING,
                        message = stringResource(R.string.ui_settings_option_enableAppLock_enrollmentRequired),
                        density = VisualDensity.COMPACT,
                    )
                }
            }
        },
        leading = {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
            )
        },
        trailing = {
            val title = stringResource(R.string.identityVerificationRequired_title)
            val subtitle = stringResource(R.string.identityVerificationRequired_subtitle)

            Switch(
                checked = settings.isAppLockEnabled(),
                enabled = appLockSupport === AppLockHelper.SupportType.AVAILABLE,
                onCheckedChange = {
                    scope.launch {
                        val authenticationSuccessful = AppLockHelper.authenticate(
                            context,
                            title = title,
                            subtitle = subtitle,
                        ).await()

                        if (!authenticationSuccessful) {
                            return@launch
                        }

                        dataStore.updateData {
                            it.setAppLockSettings(
                                if (it.appLockSettings == null)
                                    AppLockSettings.getDefaultInstance()
                                else
                                    null
                            )
                        }
                    }
                }
            )
        }
    )
}