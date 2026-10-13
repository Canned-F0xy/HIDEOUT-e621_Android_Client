package com.CannedF0xy.hideout.vpn

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel

object VpnManager {
    private var backend: GoBackend? = null

    @Volatile
    var hasBeenUp = false
        private set

    @Volatile
    var stoppedByUser = false
        private set

    fun shouldAutoReconnect(): Boolean = hasBeenUp && !stoppedByUser
    var vpnState = mutableStateOf(Tunnel.State.DOWN)
        private set

    val tunnel = object : Tunnel {
        override fun getName() = "HideoutTunnel"
        override fun onStateChange(state: Tunnel.State) {
            if (state == Tunnel.State.UP) {
                hasBeenUp = true
                stoppedByUser = false
            }
            Handler(Looper.getMainLooper()).post { vpnState.value = state }
        }
    }

    fun getBackend(context: Context): GoBackend {
        if (backend == null) {
            backend = GoBackend(context.applicationContext)
        }
        return backend!!
    }

    fun stopVpnSync(context: Context) {
        stoppedByUser = true
        try {
            getBackend(context).setState(tunnel, Tunnel.State.DOWN, null)
        } catch (e: Exception) {}
    }
}
