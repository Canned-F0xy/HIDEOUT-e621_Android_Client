package com.CannedF0xy.hideout.network.model

import com.google.gson.JsonElement

data class E621Response(val posts: List<Post>?)

data class Post(
    val id: Int,
    val file: FileData?,
    val preview: PreviewData?,
    val sample: SampleData?,
    val tags: TagsData?,
    val score: ScoreData?,
    val description: String?,
    val sources: List<String>? = emptyList(),
    val is_favorited: Boolean? = false,
    val relationships: RelationshipsData?,
    val duration: Double? = null,
    val pools: List<Int>? = emptyList(),
    val rating: String? = null,
    val created_at: String? = null
) {
    fun allTags(): Set<String> {
        val t = tags ?: return emptySet()
        val result = HashSet<String>()
        t.general?.let { result.addAll(it) }
        t.artist?.let { result.addAll(it) }
        t.character?.let { result.addAll(it) }
        t.copyright?.let { result.addAll(it) }
        t.species?.let { result.addAll(it) }
        t.meta?.let { result.addAll(it) }
        t.lore?.let { result.addAll(it) }
        t.invalid?.let { result.addAll(it) }
        t.contributor?.let { result.addAll(it) }
        return result
    }

    fun getAllRelatedIdsQuery(): String? {
        val rel = relationships ?: return null
        val ids = mutableListOf<Int>()
        rel.parent_id?.let { ids.add(it) }
        if (rel.has_children && rel.children.isNotEmpty()) {
            ids.addAll(rel.children)
        }
        if (rel.parent_id == null && rel.has_children) {
            ids.add(id)
        }
        if (ids.isEmpty()) return null
        return "id:" + ids.distinct().joinToString(",")
    }
}

data class RelationshipsData(
    val parent_id: Int?,
    val has_children: Boolean,
    val children: List<Int> = emptyList()
)

data class FileData(val url: String?, val ext: String?, val md5: String?, val width: Int? = null, val height: Int? = null)
data class PreviewData(val url: String?)
data class SampleData(
    val url: String?,
    val width: Int? = null,
    val height: Int? = null,
    val alternates: JsonElement? = null
) {
    fun videoVariants(): List<VideoVariant> {
        val root = alternates ?: return emptyList()
        val found = mutableListOf<VideoVariant>()
        collectVariants(root, null, found)
        return found.distinctBy { it.url }
    }

    private fun collectVariants(element: JsonElement, keyName: String?, out: MutableList<VideoVariant>) {
        if (!element.isJsonObject) return
        if (keyName != null && keyName.equals("original", ignoreCase = true)) return
        val obj = element.asJsonObject
        val urls = mutableListOf<String>()
        obj.get("url")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.let { urls.add(it.asString) }
        obj.get("urls")?.takeIf { it.isJsonArray }?.asJsonArray?.forEach { item ->
            if (item.isJsonPrimitive && item.asJsonPrimitive.isString) urls.add(item.asString)
        }
        val playable = urls.filter { it.endsWith(".mp4", ignoreCase = true) || it.endsWith(".webm", ignoreCase = true) }
        val chosen = playable.firstOrNull { it.endsWith(".mp4", ignoreCase = true) } ?: playable.firstOrNull()
        if (chosen != null) {
            val heightValue = obj.get("height")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt
                ?: keyName?.removeSuffix("p")?.toIntOrNull()
                ?: 0
            out.add(VideoVariant(heightValue, chosen))
        }
        for ((childKey, child) in obj.entrySet()) {
            if (child.isJsonObject) collectVariants(child, childKey, out)
        }
    }
}

data class VideoVariant(val height: Int, val url: String)
data class TagsData(
    val general: List<String>? = emptyList(),
    val artist: List<String>? = emptyList(),
    val character: List<String>? = emptyList(),
    val copyright: List<String>? = emptyList(),
    val species: List<String>? = emptyList(),
    val meta: List<String>? = emptyList(),
    val lore: List<String>? = emptyList(),
    val invalid: List<String>? = emptyList(),
    val contributor: List<String>? = emptyList()
)
data class ScoreData(val total: Int? = 0)

data class AutocompleteTag(val id: Int, val name: String, val post_count: Int, val category: Int)

data class Comment(
    val id: Int? = null,
    val post_id: Int? = null,
    val creator_name: String? = null,
    val body: String? = null,
    val score: Int = 0,
    val created_at: String? = null
)

data class Pool(
    val id: Int,
    val name: String? = null,
    val post_ids: List<Int>? = emptyList(),
    val post_count: Int? = 0
) {
    fun displayName(): String = (name ?: "Pool #$id").replace('_', ' ')
}

data class VoteResponse(
    val score: Int? = null,
    val up: Int? = null,
    val down: Int? = null,
    val our_score: Int? = null
)
