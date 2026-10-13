package com.CannedF0xy.hideout.follow

import android.content.Context
import androidx.core.app.NotificationCompat
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.i18n.AppStrings
import com.CannedF0xy.hideout.i18n.EnStrings
import com.CannedF0xy.hideout.i18n.KoStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.SearchQueryBuilder
import com.CannedF0xy.hideout.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object FollowChecker {
    private const val CHECK_LIMIT = 50
    private const val NOTIFICATION_BASE_ID = 8000
    private val mutex = Mutex()

    suspend fun fetchNewestId(tag: String, isNsfwEnabled: Boolean, blacklistedTags: List<String>): Int? = withContext(Dispatchers.IO) {
        try {
            val query = SearchQueryBuilder.build(tag, isNsfwEnabled, blacklistedTags)
            NetworkModule.api!!.getPosts(tags = query.tags, limit = 1, page = 1).posts?.firstOrNull()?.id ?: 0
        } catch (e: Exception) {
            null
        }
    }

    suspend fun check(context: Context): Int = mutex.withLock {
        withContext(Dispatchers.IO) {
            val prefs = AppPreferences(context.applicationContext)
            val followed = prefs.followedTags.first()
            if (followed.isEmpty()) return@withContext 0

            applySavedCredentials(prefs)
            val strings: AppStrings = if (prefs.appLanguage.first() == "en") EnStrings else KoStrings
            val isNsfwEnabled = prefs.isNsfwEnabled.first()
            val blacklist = prefs.blacklistedTags.first().toList()

            val updates = HashMap<String, Int>()
            var notified = 0
            for ((tag, lastSeenId) in followed) {
                try {
                    if (lastSeenId <= 0) {
                        fetchNewestId(tag, isNsfwEnabled, blacklist)?.let { updates[tag] = it }
                        continue
                    }
                    val query = SearchQueryBuilder.build("$tag id:>$lastSeenId", isNsfwEnabled, blacklist)
                    val raw = NetworkModule.api!!.getPosts(tags = query.tags, limit = CHECK_LIMIT, page = 1).posts ?: emptyList()
                    if (raw.isEmpty()) continue
                    updates[tag] = raw.maxOf { it.id }
                    val fresh = query.filter(raw)
                    if (fresh.isNotEmpty()) {
                        showNotification(context, strings, tag, fresh.size, raw.size >= CHECK_LIMIT)
                        notified++
                    }
                } catch (e: Exception) {
                }
            }
            if (updates.isNotEmpty()) prefs.updateFollowedTagIds(updates)
            notified
        }
    }

    private suspend fun applySavedCredentials(prefs: AppPreferences) {
        if (NetworkModule.username.isBlank() || NetworkModule.apiKey.isBlank()) {
            NetworkModule.username = prefs.username.first()
            NetworkModule.apiKey = prefs.apiKey.first()
        }
        if (NetworkModule.cfClearance.isBlank()) {
            NetworkModule.cfClearance = prefs.cfClearance.first()
        }
    }

    private fun showNotification(context: Context, strings: AppStrings, tag: String, count: Int, hasMore: Boolean) {
        val id = NOTIFICATION_BASE_ID + (tag.hashCode() and 0xFFF)
        val countText = if (hasMore) "$count+" else "$count"
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_FOLLOW)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(strings.followNotificationTitle(tag))
            .setContentText(strings.followNotificationText(countText))
            .setAutoCancel(true)
            .setContentIntent(NotificationHelper.openAppIntent(context, id, tag))
            .build()
        NotificationHelper.notify(context, id, notification)
    }
}
