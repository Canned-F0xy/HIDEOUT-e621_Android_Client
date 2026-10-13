package com.CannedF0xy.hideout.vpn

import com.wireguard.android.backend.Tunnel
import java.io.IOException

class VpnBlockedException : IOException("VPN tunnel is down")

object VpnGuard {
    @Volatile
    var killSwitchEnabled = false

    @Volatile
    var usesInAppTunnel = false

    fun shouldBlock(): Boolean {
        return killSwitchEnabled && usesInAppTunnel && VpnManager.vpnState.value != Tunnel.State.UP
    }
}
