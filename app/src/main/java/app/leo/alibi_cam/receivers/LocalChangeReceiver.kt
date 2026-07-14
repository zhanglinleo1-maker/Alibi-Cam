package app.leo.alibi_cam.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import app.leo.alibi_cam.NotificationHelper

class LocalChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createChannels(context!!)
        }
    }
}