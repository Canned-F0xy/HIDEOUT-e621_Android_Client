package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.E621Response
import com.CannedF0xy.hideout.network.model.Post
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class E621ApiException(message: String) : IOException(message)

suspend fun E621ApiService.getPosts(tags: String, limit: Int = 20, page: Int = 1): E621Response {
    val body = getPostsRaw(tags = tags, limit = limit, page = page)
    return withContext(Dispatchers.IO) { PostsResponseParser.parse(body.string()) }
}

object PostsResponseParser {
    private val gson = Gson()

    fun parse(text: String): E621Response {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return E621Response(emptyList())
        val root = try {
            JsonParser().parse(trimmed)
        } catch (e: Exception) {
            throw E621ApiException("Invalid response from e621")
        }
        return when {
            root.isJsonArray -> E621Response(parsePosts(root.asJsonArray))
            root.isJsonObject -> {
                val obj = root.asJsonObject
                val posts = obj.get("posts")
                when {
                    posts != null && posts.isJsonArray -> E621Response(parsePosts(posts.asJsonArray))
                    posts != null && posts.isJsonObject -> E621Response(listOfNotNull(parsePost(posts)))
                    obj.get("post")?.isJsonObject == true -> E621Response(listOfNotNull(parsePost(obj.get("post"))))
                    obj.has("id") && (obj.has("file") || obj.has("files")) -> E621Response(listOfNotNull(parsePost(obj)))
                    obj.get("success")?.takeIf { it.isJsonPrimitive }?.asBoolean == false -> {
                        val message = obj.get("message")?.takeIf { it.isJsonPrimitive }?.asString
                            ?: obj.get("reason")?.takeIf { it.isJsonPrimitive }?.asString
                            ?: "e621 request failed"
                        throw E621ApiException(message)
                    }
                    else -> E621Response(emptyList())
                }
            }
            else -> E621Response(emptyList())
        }
    }

    private fun parsePosts(array: JsonArray): List<Post> = array.mapNotNull { parsePost(it) }

    private fun parsePost(element: JsonElement): Post? {
        if (!element.isJsonObject) return null
        return try {
            gson.fromJson(normalizeV2(element.asJsonObject), Post::class.java)?.takeIf { it.id > 0 }
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeV2(source: JsonObject): JsonObject {
        val files = source.get("files")?.takeIf { it.isJsonObject }?.asJsonObject
        val tagsIsList = source.get("tags")?.isJsonArray == true
        val stats = source.get("stats")?.takeIf { it.isJsonObject }?.asJsonObject
        if (files == null && !tagsIsList && stats == null) return source

        val post = source.deepCopy()
        if (files != null) {
            val meta = files.objectOrNull("meta")
            val original = files.objectOrNull("original")
            val preview = files.objectOrNull("preview")
            val sample = files.objectOrNull("sample")

            if (post.get("file")?.isJsonObject != true) {
                post.add("file", JsonObject().apply {
                    copyString(original, "url", this, "url")
                    copyString(meta, "ext", this, "ext")
                    copyString(meta, "md5", this, "md5")
                    copyNumber(original, "width", this, "width")
                    copyNumber(original, "height", this, "height")
                    if (!has("width")) copyNumber(meta, "width", this, "width")
                    if (!has("height")) copyNumber(meta, "height", this, "height")
                })
            }
            if (post.get("preview")?.isJsonObject != true) {
                post.add("preview", JsonObject().apply {
                    firstString(preview, "jpg", "webp", "url")?.let { addProperty("url", it) }
                })
            }
            if (post.get("sample")?.isJsonObject != true) {
                post.add("sample", JsonObject().apply {
                    firstString(sample, "jpg", "webp", "url")?.let { addProperty("url", it) }
                    copyNumber(sample, "width", this, "width")
                    copyNumber(sample, "height", this, "height")
                    sample?.get("alternates")?.let { add("alternates", it) }
                })
            }
            if (!post.has("duration")) meta?.get("duration")?.takeIf { it.isJsonPrimitive }?.let { post.add("duration", it) }
        }

        if (tagsIsList) {
            val list = JsonArray()
            source.getAsJsonArray("tags").forEach { if (it.isJsonPrimitive) list.add(it) }
            post.add("tags", JsonObject().apply { add("general", list) })
        }

        if (stats != null) {
            if (post.get("score")?.isJsonObject != true) {
                val score = stats.get("score")
                when {
                    score != null && score.isJsonObject -> post.add("score", score)
                    score != null && score.isJsonPrimitive -> post.add("score", JsonObject().apply { add("total", score) })
                }
            }
            if (!post.has("is_favorited")) stats.get("is_favorited")?.takeIf { it.isJsonPrimitive }?.let { post.add("is_favorited", it) }
        }
        return post
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? = get(key)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun firstString(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.get(key)
            if (value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString && value.asString.isNotBlank()) return value.asString
        }
        return null
    }

    private fun copyString(from: JsonObject?, fromKey: String, to: JsonObject, toKey: String) {
        val value = from?.get(fromKey) ?: return
        if (value.isJsonPrimitive && value.asJsonPrimitive.isString) to.add(toKey, value)
    }

    private fun copyNumber(from: JsonObject?, fromKey: String, to: JsonObject, toKey: String) {
        val value = from?.get(fromKey) ?: return
        if (value.isJsonPrimitive && value.asJsonPrimitive.isNumber) to.add(toKey, value)
    }
}
