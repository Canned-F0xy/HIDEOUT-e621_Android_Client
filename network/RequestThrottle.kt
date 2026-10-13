package com.CannedF0xy.hideout.network

import android.os.SystemClock

object RequestThrottle {
    private const val MIN_INTERVAL_MS = 500L
    private val lock = Object()
    private var lastRequestAt = 0L

    fun awaitTurn() {
        synchronized(lock) {
            val now = SystemClock.elapsedRealtime()
            val wait = lastRequestAt + MIN_INTERVAL_MS - now
            if (wait > 0) Thread.sleep(wait)
            lastRequestAt = SystemClock.elapsedRealtime()
        }
    }
}
