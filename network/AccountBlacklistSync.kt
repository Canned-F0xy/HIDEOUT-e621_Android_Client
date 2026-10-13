package com.CannedF0xy.hideout.network

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BlacklistSyncResult(
    val importedTags: List<String>,
    val skippedLines: Int
)

object AccountBlacklistSync {
    suspend fun fetch(username: String): BlacklistSyncResult? = withContext(Dispatchers.IO) {
        try {
            val api = NetworkModule.api ?: return@withContext null
            val usersJson = JsonParser().parse(api.searchUsers(username).string())
            if (!usersJson.isJsonArray || usersJson.asJsonArray.size() == 0) return@withContext null
            val user = usersJson.asJsonArray.firstOrNull { element ->
                element.isJsonObject && element.asJsonObject.get("name")?.asString.equals(username, ignoreCase = true)
            } ?: usersJson.asJsonArray[0]
            val userId = user.asJsonObject.get("id")?.asInt ?: return@withContext null

            val detail = JsonParser().parse(api.getUser(userId).string())
            if (!detail.isJsonObject) return@withContext null
            val raw = detail.asJsonObject.get("blacklisted_tags")
            if (raw == null || raw.isJsonNull) return@withContext null

            val imported = mutableListOf<String>()
            var skipped = 0
            raw.asString.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
                val isSingleTag = !line.contains(" ") && !line.startsWith("-") && !line.startsWith("~")
                if (isSingleTag) imported.add(line.lowercase()) else skipped++
            }
            BlacklistSyncResult(imported.distinct(), skipped)
        } catch (e: Exception) {
            null
        }
    }
}
