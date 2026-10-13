package com.CannedF0xy.hideout.security

import android.os.SystemClock
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest
import java.security.SecureRandom

object AppLockManager {
    const val PIN_LENGTH = 4
    private const val RELOCK_GRACE_MS = 10_000L
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val COOLDOWN_MS = 30_000L

    var isLocked by mutableStateOf(false)
        private set
    var isEnabled = false
    var cooldownUntil by mutableStateOf(0L)
        private set

    private var backgroundedAt = 0L
    private var failedAttempts = 0

    fun lockNow() {
        if (isEnabled) isLocked = true
    }

    fun unlock() {
        isLocked = false
        failedAttempts = 0
    }

    fun onAppBackgrounded() {
        backgroundedAt = SystemClock.elapsedRealtime()
    }

    fun onAppForegrounded() {
        if (!isEnabled || backgroundedAt == 0L) return
        if (SystemClock.elapsedRealtime() - backgroundedAt >= RELOCK_GRACE_MS) isLocked = true
        backgroundedAt = 0L
    }

    fun remainingCooldownMs(): Long = (cooldownUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0L)

    fun verify(pin: String, record: String): Boolean {
        if (remainingCooldownMs() > 0) return false
        val ok = matches(pin, record)
        if (ok) {
            unlock()
        } else {
            failedAttempts++
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                failedAttempts = 0
                cooldownUntil = SystemClock.elapsedRealtime() + COOLDOWN_MS
            }
        }
        return ok
    }

    fun createRecord(pin: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltText = Base64.encodeToString(salt, Base64.NO_WRAP)
        return "$saltText:${hash(saltText, pin)}"
    }

    private fun matches(pin: String, record: String): Boolean {
        val parts = record.split(":")
        if (parts.size != 2) return false
        return MessageDigest.isEqual(hash(parts[0], pin).toByteArray(), parts[1].toByteArray())
    }

    private fun hash(salt: String, pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest("$salt$pin".toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
