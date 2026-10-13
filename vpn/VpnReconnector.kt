package com.CannedF0xy.hideout.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object VpnReconnector {
    private const val MAX_ATTEMPTS = 3
    private const val BASE_DELAY_MS = 2000L

    suspend fun reconnect(context: Context, configText: String): Boolean {
        for (attempt in 1..MAX_ATTEMPTS) {
            delay(BASE_DELAY_MS * attempt)
            if (VpnManager.vpnState.value == Tunnel.State.UP) return true
            if (!VpnManager.shouldAutoReconnect()) return false
            if (VpnService.prepare(context) != null) return false
            val success = withContext(Dispatchers.IO) {
                try {
                    val config = Config.parse(configText.byteInputStream(Charsets.UTF_8))
                    VpnManager.getBackend(context).setState(VpnManager.tunnel, Tunnel.State.UP, config)
                    context.startService(Intent(context, VpnCleanupService::class.java))
                    true
                } catch (e: Exception) {
                    false
                }
            }
            if (success) return true
        }
        return false
    }
}
