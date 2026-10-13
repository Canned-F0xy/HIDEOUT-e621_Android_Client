package com.CannedF0xy.hideout.util

import android.content.Context
import android.net.ConnectivityManager

object DataSaver {
    fun isMeteredNetwork(context: Context): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        return manager.isActiveNetworkMetered
    }

    fun shouldSave(context: Context, isEnabled: Boolean): Boolean = isEnabled && isMeteredNetwork(context)
}
