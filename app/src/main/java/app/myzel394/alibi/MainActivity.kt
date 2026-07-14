package app.myzel394.alibi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import app.myzel394.alibi.db.AppSettingsSerializer
import app.myzel394.alibi.ui.AsLockedApp
import app.myzel394.alibi.ui.LockedAppHandlers
import app.myzel394.alibi.ui.Navigation
import app.myzel394.alibi.ui.theme.AlibiTheme

const val SETTINGS_FILE = "settings.json"
val Context.dataStore by dataStore(
    fileName = SETTINGS_FILE,
    serializer = AppSettingsSerializer()
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Proactively request notification permission (Android 13+)
        requestNotificationPermission()

        setContent {
            AlibiTheme {
                LockedAppHandlers()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background
                        )
                ) {
                    AsLockedApp {
                        Navigation()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                )
            }
        }
    }
}
