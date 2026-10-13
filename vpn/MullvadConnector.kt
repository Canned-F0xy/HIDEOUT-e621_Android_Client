package com.CannedF0xy.hideout.vpn

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.*
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.network.MullvadNetwork
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

suspend fun autoConnectMullvad(
    accountNumber: String,
    context: Context,
    appPrefs: AppPreferences
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val keyPair = KeyPair()
            val privKey = keyPair.privateKey.toBase64()
            val pubKey = keyPair.publicKey.toBase64()
            appPrefs.setMullvadKeys(privKey, pubKey)

            val cleanAccount = accountNumber.replace(Regex("[^0-9]"), "")
            val ipResponse = MullvadNetwork.api.registerKey(cleanAccount, pubKey)

            if (!ipResponse.isSuccessful) return@withContext false

            val tunnelAddresses = ipResponse.body()?.string()?.trim() ?: ""
            if (tunnelAddresses.isEmpty()) return@withContext false

            val relays = MullvadNetwork.api.getRelays()
            val targetServer = relays.firstOrNull { it.country_code == "jp" && it.active }
                ?: relays.first { it.active }

            val configText = """
                [Interface]
                PrivateKey = $privKey
                Address = $tunnelAddresses
                DNS = 10.64.0.1
                
                [Peer]
                PublicKey = ${targetServer.pubkey}
                Endpoint = ${targetServer.ipv4_addr_in}:51820
                AllowedIPs = 0.0.0.0/0
            """.trimIndent()

            appPrefs.setVpnConfig(configText)

            val config = Config.parse(configText.byteInputStream(Charsets.UTF_8))
            val backend = VpnManager.getBackend(context)

            backend.setState(VpnManager.tunnel, Tunnel.State.UP, config)
            context.startService(Intent(context, VpnCleanupService::class.java))

            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }
}
