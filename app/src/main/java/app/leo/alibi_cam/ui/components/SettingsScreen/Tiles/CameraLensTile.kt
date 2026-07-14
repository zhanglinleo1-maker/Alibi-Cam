package app.leo.alibi_cam.ui.components.SettingsScreen.Tiles

import androidx.camera.core.CameraSelector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.leo.alibi_cam.R
import app.leo.alibi_cam.dataStore
import app.leo.alibi_cam.db.AppSettings
import app.leo.alibi_cam.db.VideoRecorderSettings
import app.leo.alibi_cam.ui.components.atoms.ExampleListRoulette
import app.leo.alibi_cam.ui.components.atoms.SettingsTile
import app.leo.alibi_cam.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraLensTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    val options = listOf(
        "auto" to stringResource(R.string.ui_settings_value_cameraLens_auto),
        "ultrawide" to stringResource(R.string.ui_settings_value_cameraLens_ultrawide),
        "main" to stringResource(R.string.ui_settings_value_cameraLens_main),
        "front" to stringResource(R.string.ui_settings_value_cameraLens_front),
    )

    val currentValue = settings.videoRecorderSettings.cameraLens ?: "auto"
    val currentLabel = options.firstOrNull { it.first == currentValue }?.second
        ?: stringResource(R.string.ui_settings_value_auto_label)

    fun updateValue(value: String?) {
        scope.launch {
            dataStore.updateData {
                it.setVideoRecorderSettings(
                    it.videoRecorderSettings.setCameraLens(value)
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_cameraLensTile_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.CameraAlt)
                    .asPainterResource(),
                contentDescription = null,
            ),
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = options.map { (value, label) ->
                ListOption(
                    titleText = label,
                    selected = (settings.videoRecorderSettings.cameraLens ?: "auto") == value,
                )
            }
        ) { index, _ ->
            val selectedValue = options[index].first
            updateValue(if (selectedValue == "auto") null else selectedValue)
        },
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_cameraLensTile_title),
        leading = {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = showDialog::show,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(currentLabel)
            }
        },
        extra = {
            ExampleListRoulette(
                items = listOf(null, "auto", "ultrawide", "main", "front"),
                onItemSelected = ::updateValue,
            ) {
                Text(stringResource(R.string.ui_settings_value_auto_label))
            }
        },
    )
}
