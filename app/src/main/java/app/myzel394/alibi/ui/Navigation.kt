package app.myzel394.alibi.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.alibi.BuildConfig
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.helpers.UpdateHelper
import app.myzel394.alibi.helpers.UpdateInfo
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.screens.AboutScreen
import app.myzel394.alibi.ui.screens.CustomRecordingNotificationsScreen
import app.myzel394.alibi.ui.screens.RecorderScreen
import app.myzel394.alibi.ui.screens.SettingsScreen
import app.myzel394.alibi.ui.screens.WelcomeScreen
import kotlinx.coroutines.launch

const val SCALE_IN = 1.25f
const val DEBUG_SKIP_WELCOME = false;

@Composable
fun Navigation(
    audioRecorder: AudioRecorderModel = viewModel(),
    videoRecorder: VideoRecorderModel = viewModel(),
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStore = context.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    DisposableEffect(Unit) {
        audioRecorder.bindToService(context)
        videoRecorder.bindToService(context)

        onDispose {
            audioRecorder.unbindFromService(context)
            videoRecorder.unbindFromService(context)
        }
    }

    // ── 首次安装遥测上报 ──
    LaunchedEffect(settings) {
        if (settings.installId == null) {
            val newId = java.util.UUID.randomUUID().toString().take(8)
            dataStore.updateData { it.setInstallId(newId) }
            app.myzel394.alibi.helpers.TelemetryHelper.reportNewInstall(
                appVersion = app.myzel394.alibi.BuildConfig.VERSION_NAME,
                installId = newId,
            )
        }
    }

    // ── 自动检查更新（每天最多一次）──
    val scope = rememberCoroutineScope()
    var autoUpdateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(settings) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        if (now - settings.lastUpdateCheckTime < oneDayMs) return@LaunchedEffect

        // 更新时间戳，避免重复检查
        dataStore.updateData { it.setLastUpdateCheckTime(now) }

        // 更新检查
        val info = UpdateHelper.checkForUpdate(BuildConfig.VERSION_CODE)
        if (info != null && settings.dismissedUpdateVersionCode < info.versionCode) {
            autoUpdateInfo = info
        }

        // 广告配置同步
        val adConfig = UpdateHelper.fetchAdConfig()
        dataStore.updateData {
            it.setAdConfig(adConfig.enabled, adConfig.version, adConfig.imageUrl, adConfig.clickUrl)
        }
    }

    // ── Auto-record on app open ──
    LaunchedEffect(Unit) {
        if (videoRecorder.isInRecording) return@LaunchedEffect
        if (!settings.autoRecordOnAppOpen) return@LaunchedEffect

        navController.navigate(Screen.AudioRecorder.route) {
            popUpTo(0) { inclusive = true }
        }
        videoRecorder.init(context, settings)
        videoRecorder.startRecording(context, settings)
    }

    NavHost(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = if (settings.hasSeenOnboarding || DEBUG_SKIP_WELCOME) Screen.AudioRecorder.route else Screen.Welcome.route,
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToAudioRecorderScreen = {
                    val mainHandler = ContextCompat.getMainExecutor(context)

                    mainHandler.execute {
                        navController.navigate(Screen.AudioRecorder.route)
                    }
                },
            )
        }
        composable(
            Screen.AudioRecorder.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Welcome.route -> null
                    else -> scaleIn(initialScale = SCALE_IN) + fadeIn()
                }
            },
            exitTransition = {
                scaleOut(targetScale = SCALE_IN) + fadeOut(tween(durationMillis = 150))
            }
        ) {
            RecorderScreen(
                onNavigateToSettingsScreen = {
                    navController.navigate(Screen.Settings.route)
                },
                audioRecorder = audioRecorder,
                videoRecorder = videoRecorder,
                settings = settings,
            )
        }
        composable(
            Screen.Settings.route,
            enterTransition = {
                scaleIn(initialScale = 1 / SCALE_IN) + fadeIn()
            },
            exitTransition = {
                scaleOut(targetScale = 1 / SCALE_IN) + fadeOut(tween(durationMillis = 150))
            }
        ) {
            SettingsScreen(
                onBackNavigate = navController::popBackStack,
                onNavigateToCustomRecordingNotifications = {
                    navController.navigate(Screen.CustomRecordingNotifications.route)
                },
                onNavigateToAboutScreen = { navController.navigate(Screen.About.route) },
                audioRecorder = audioRecorder,
                videoRecorder = videoRecorder,
            )
        }
        composable(
            Screen.CustomRecordingNotifications.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it -> it / 2 }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it -> it / 2 }
                ) + fadeOut(tween(150))
            }
        ) {
            CustomRecordingNotificationsScreen(
                onBackNavigate = navController::popBackStack
            )
        }
        composable(
            Screen.About.route,
            enterTransition = {
                scaleIn()
            },
            exitTransition = {
                scaleOut() + fadeOut(tween(150))
            }
        ) {
            AboutScreen(
                onBackNavigate = navController::popBackStack,
            )
        }
    }

    // ── 自动更新对话框 ──
    autoUpdateInfo?.let { info ->
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = {
                scope.launch {
                    dataStore.updateData {
                        it.setDismissedUpdateVersionCode(info.versionCode)
                    }
                }
                autoUpdateInfo = null
            },
            icon = {
                Icon(
                    Icons.Default.SystemUpdateAlt,
                    contentDescription = null,
                )
            },
            title = {
                Text(stringResource(R.string.ui_update_dialog_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.ui_update_dialog_message,
                        info.versionName,
                        info.message
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        uriHandler.openUri(info.downloadUrl)
                        scope.launch {
                            dataStore.updateData {
                                it.setDismissedUpdateVersionCode(info.versionCode)
                            }
                        }
                        autoUpdateInfo = null
                    }
                ) {
                    Text(stringResource(R.string.ui_update_dialog_download))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dataStore.updateData {
                                it.setDismissedUpdateVersionCode(info.versionCode)
                            }
                        }
                        autoUpdateInfo = null
                    }
                ) {
                    Text(stringResource(R.string.ui_update_dialog_dismiss))
                }
            }
        )
    }
}
