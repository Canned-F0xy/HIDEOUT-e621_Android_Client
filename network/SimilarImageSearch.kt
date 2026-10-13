package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.Post
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SimilarImageSearch {
    private const val MAX_RESULTS = 40

    suspend fun findSimilarQuery(post: Post): String? = withContext(Dispatchers.IO) {
        val api = NetworkModule.api ?: return@withContext null
        var ids = try {
            parseIds(api.iqdbByPost(post.id, post.id).string())
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) {
            val imageUrl = post.preview?.url ?: post.sample?.url ?: return@withContext null
            ids = try {
                parseIds(api.iqdbByUrl(imageUrl, imageUrl).string())
            } catch (e: Exception) {
                emptyList()
            }
        }
        val others = ids.filter { it != post.id }.distinct().take(MAX_RESULTS)
        if (others.isEmpty()) null else "id:" + others.joinToString(",")
    }

    private fun parseIds(body: String): List<Int> {
        val root = JsonParser().parse(body)
        val array = when {
            root.isJsonArray -> root.asJsonArray
            root.isJsonObject && root.asJsonObject.has("posts") && root.asJsonObject.get("posts").isJsonArray -> root.asJsonObject.getAsJsonArray("posts")
            else -> return emptyList()
        }
        return array.mapNotNull { extractId(it) }
    }

    private fun extractId(element: JsonElement): Int? {
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        obj.get("post_id")?.takeIf { it.isJsonPrimitive }?.let { return it.asInt }
        val nested = obj.get("post")
        if (nested != null && nested.isJsonObject) {
            val inner = nested.asJsonObject
            inner.get("id")?.takeIf { it.isJsonPrimitive }?.let { return it.asInt }
            val posts = inner.get("posts")
            if (posts != null && posts.isJsonObject) {
                posts.asJsonObject.get("id")?.takeIf { it.isJsonPrimitive }?.let { return it.asInt }
            }
        }
        obj.get("id")?.takeIf { it.isJsonPrimitive }?.let { return it.asInt }
        return null
    }
}
