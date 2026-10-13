package com.CannedF0xy.hideout.vpn

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VpnCleanupService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Thread {
            VpnManager.stopVpnSync(applicationContext)
            stopSelf()
        }.start()
    }
}
