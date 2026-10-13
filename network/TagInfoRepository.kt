package com.CannedF0xy.hideout.network

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TagInfo(
    val name: String,
    val postCount: Int?,
    val category: Int?,
    val wikiText: String?
)

object TagInfoRepository {
    private const val MAX_WIKI_LENGTH = 1500

    suspend fun load(tag: String): TagInfo = withContext(Dispatchers.IO) {
        val api = NetworkModule.api!!
        var postCount: Int? = null
        var category: Int? = null
        try {
            val tags = JsonParser().parse(api.searchTags(tag).string())
            if (tags.isJsonArray && tags.asJsonArray.size() > 0) {
                val obj = tags.asJsonArray[0].asJsonObject
                postCount = obj.get("post_count")?.takeIf { it.isJsonPrimitive }?.asInt
                category = obj.get("category")?.takeIf { it.isJsonPrimitive }?.asInt
            }
        } catch (e: Exception) {
        }
        var wiki: String? = null
        try {
            val pages = JsonParser().parse(api.searchWiki(tag).string())
            if (pages.isJsonArray && pages.asJsonArray.size() > 0) {
                val body = pages.asJsonArray[0].asJsonObject.get("body")
                if (body != null && body.isJsonPrimitive) wiki = cleanDText(body.asString)
            }
        } catch (e: Exception) {
        }
        TagInfo(tag, postCount, category, wiki?.takeIf { it.isNotBlank() })
    }

    private fun cleanDText(raw: String): String {
        var text = raw.replace("\r\n", "\n")
        text = Regex("\\[\\[([^\\]|]+)\\|([^\\]]+)]]").replace(text) { it.groupValues[2] }
        text = Regex("\\[\\[([^\\]]+)]]").replace(text) { it.groupValues[1].replace('_', ' ') }
        text = Regex("\\{\\{([^}]+)}}").replace(text) { it.groupValues[1] }
        text = Regex("\"([^\"]+)\":\\S+").replace(text) { it.groupValues[1] }
        text = Regex("(?m)^h[1-6]\\.\\s*").replace(text, "")
        text = Regex("\\[/?[a-zA-Z]+(=[^\\]]*)?]").replace(text, "")
        text = Regex("\n{3,}").replace(text, "\n\n").trim()
        return if (text.length > MAX_WIKI_LENGTH) text.take(MAX_WIKI_LENGTH) + "…" else text
    }
}
