package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import kotlinx.coroutines.launch

@Composable
fun AutoStopSensorTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_autoStopSensor_title),
        description = stringResource(R.string.ui_settings_option_autoStopSensor_description),
        leading = {
            Icon(
                Icons.Outlined.ScreenRotation,
                contentDescription = null,
            )
        },
        trailing = {
            Switch(
                checked = settings.autoStopSensorEnabled,
                onCheckedChange = {
                    scope.launch {
                        dataStore.updateData {
                            it.setAutoStopSensorEnabled(it.autoStopSensorEnabled.not())
                        }
                    }
                }
            )
        },
        extra = if (settings.autoStopSensorEnabled) {
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // ── Sensitivity selector ──
                    Text(
                        text = stringResource(R.string.ui_settings_option_autoStopSensor_sensitivity),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        val options = listOf(
                            "strict" to stringResource(R.string.ui_settings_option_autoStopSensor_sensitivity_strict),
                            "standard" to stringResource(R.string.ui_settings_option_autoStopSensor_sensitivity_standard),
                            "relaxed" to stringResource(R.string.ui_settings_option_autoStopSensor_sensitivity_relaxed),
                        )
                        options.forEach { (key, label) ->
                            FilterChip(
                                selected = settings.autoStopSensitivity == key,
                                onClick = {
                                    scope.launch {
                                        dataStore.updateData {
                                            it.setAutoStopSensitivity(key)
                                        }
                                    }
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Screen-off requirement check ──
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = settings.autoStopRequireScreenOff,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    dataStore.updateData {
                                        it.setAutoStopRequireScreenOff(checked)
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.ui_settings_option_autoStopSensor_screenOff),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        } else null,
    )
}
