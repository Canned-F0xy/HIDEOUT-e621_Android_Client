package com.CannedF0xy.hideout.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.CannedF0xy.hideout.network.model.Post

class E621PagingSource(
    private val query: String,
    private val isNsfwEnabled: Boolean,
    private val blacklistedTags: List<String>,
    private val onAuthError: () -> Unit,
    private val onCloudflareBlocked: () -> Unit
) : PagingSource<Int, Post>() {

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: 1
        return try {
            val searchQuery = SearchQueryBuilder.build(query, isNsfwEnabled, blacklistedTags)

            val response = NetworkModule.api!!.getPosts(
                tags = searchQuery.tags,
                limit = params.loadSize.coerceAtMost(30),
                page = page
            )
            val rawPosts = response.posts ?: emptyList()
            val posts = searchQuery.filter(rawPosts)

            LoadResult.Page(
                data = posts,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (rawPosts.isEmpty() || rawPosts.size < 10) null else page + 1
            )
        } catch (e: Exception) {
            if (e is retrofit2.HttpException && e.code() == 401) onAuthError()
            if (e is retrofit2.HttpException && e.code() == 403) onCloudflareBlocked()
            LoadResult.Error(e)
        }
    }
}
