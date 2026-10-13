package com.CannedF0xy.hideout.download

import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.SearchQueryBuilder
import com.CannedF0xy.hideout.network.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BatchDownloader {
    private const val PAGE_LIMIT = 320

    suspend fun collectSearchPosts(
        query: String,
        isNsfwEnabled: Boolean,
        blacklistedTags: List<String>,
        maxCount: Int
    ): List<Post> = withContext(Dispatchers.IO) {
        val api = NetworkModule.api!!
        val searchQuery = SearchQueryBuilder.build(query, isNsfwEnabled, blacklistedTags)
        val result = LinkedHashMap<Int, Post>()
        var page = 1
        while (result.size < maxCount) {
            val raw = api.getPosts(tags = searchQuery.tags, limit = PAGE_LIMIT, page = page).posts ?: emptyList()
            searchQuery.filter(raw).forEach { if (result.size < maxCount) result[it.id] = it }
            if (raw.size < PAGE_LIMIT) break
            page++
        }
        result.values.toList()
    }

    fun toRequests(posts: List<Post>, template: String, poolId: Int? = null): List<DownloadRequest> {
        return posts.mapIndexedNotNull { index, post ->
            val file = post.file ?: return@mapIndexedNotNull null
            val url = file.url ?: return@mapIndexedNotNull null
            val ext = file.ext ?: return@mapIndexedNotNull null
            val name = if (poolId != null) {
                FileNameFormatter.formatForPool(template, post, ext, poolId, index + 1)
            } else {
                FileNameFormatter.format(template, post, ext)
            }
            DownloadRequest(url = url, fileName = name, md5 = file.md5, ext = ext)
        }
    }
}
