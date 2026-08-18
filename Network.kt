package com.CannedF0xy.hideout

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

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
    val pools: List<Int>? = emptyList()
) {
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

data class FileData(val url: String?, val ext: String?, val md5: String?)
data class PreviewData(val url: String?)
data class SampleData(val url: String?)
data class TagsData(
    val general: List<String>? = emptyList(),
    val artist: List<String>? = emptyList(),
    val character: List<String>? = emptyList(),
    val copyright: List<String>? = emptyList()
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

data class MullvadRelay(
    val hostname: String,
    val country_code: String,
    val active: Boolean,
    val ipv4_addr_in: String,
    val pubkey: String
)

data class GithubRelease(val tag_name: String, val html_url: String)

interface E621ApiService {
    @GET("posts.json")
    suspend fun getPosts(
        @Query("tags") tags: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): E621Response

    @GET("tags/autocomplete.json")
    suspend fun getAutocomplete(@Query("search[name_matches]") query: String): List<AutocompleteTag>

    @GET("comments.json")
    suspend fun getComments(
        @Query("search[post_id]") postId: Int
    ): ResponseBody

    @POST("favorites.json")
    @FormUrlEncoded
    suspend fun addFavorite(@Field("post_id") postId: Int): Response<Unit>

    @DELETE("favorites/{id}.json")
    suspend fun removeFavorite(@Path("id") postId: Int): Response<Unit>
}

interface MullvadApiService {
    @FormUrlEncoded
    @POST("wg/")
    suspend fun registerKey(
        @Field("account") account: String,
        @Field("pubkey") pubKey: String
    ): Response<ResponseBody>

    @GET("www/relays/wireguard/")
    suspend fun getRelays(): List<MullvadRelay>
}

object NetworkModule {
    private const val BASE_URL = "https://e621.net/"
    var api: E621ApiService? = null

    const val DEFAULT_USER_AGENT = "HideoutApp/1.0 (by CannedF0xy on e621)"

    var username: String = ""
    var apiKey: String = ""
    var cfClearance: String = ""

    var onCloudflareChallenge: (() -> Unit)? = null

    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("User-Agent", DEFAULT_USER_AGENT)

        if (cfClearance.isNotBlank() && cfClearance != "bypass") {
            requestBuilder.header("Cookie", cfClearance)
        }

        if (username.isNotBlank() && apiKey.isNotBlank()) {
            val credential = Credentials.basic(username, apiKey)
            requestBuilder.header("Authorization", credential)
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 403 || response.code == 503) {
            onCloudflareChallenge?.invoke()
        }

        response
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(E621ApiService::class.java)
    }
}

object MullvadNetwork {
    private const val BASE_URL = "https://api.mullvad.net/"

    val api: MullvadApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MullvadApiService::class.java)
    }
}

suspend fun checkGithubUpdate(): GithubRelease? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://api.github.com/repos/Canned-F0xy/HIDEOUT-e621_Android_Client/releases/latest")
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body?.string() ?: return@withContext null
            return@withContext Gson().fromJson(body, GithubRelease::class.java)
        }
    } catch (e: Exception) {}
    return@withContext null
}

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
            var finalTag = if (query == "#HOT#") "date:>=7_days_ago order:score" else query.trim()
            if (!isNsfwEnabled && !finalTag.contains("rating:")) {
                finalTag = if (finalTag.isEmpty()) "rating:safe" else "$finalTag rating:safe"
            }
            if (blacklistedTags.isNotEmpty()) {
                val negatedTags = blacklistedTags.joinToString(" ") { "-$it" }
                finalTag = if (finalTag.isEmpty()) negatedTags else "$finalTag $negatedTags"
            }

            val response = NetworkModule.api!!.getPosts(
                tags = finalTag,
                limit = params.loadSize.coerceAtMost(30),
                page = page
            )
            val posts = response.posts ?: emptyList()

            LoadResult.Page(
                data = posts,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (posts.isEmpty() || posts.size < 10) null else page + 1
            )
        } catch (e: Exception) {
            if (e is retrofit2.HttpException && e.code() == 401) onAuthError()
            if (e is retrofit2.HttpException && e.code() == 403) onCloudflareBlocked()
            LoadResult.Error(e)
        }
    }
}
