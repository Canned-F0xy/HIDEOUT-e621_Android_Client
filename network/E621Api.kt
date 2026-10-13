package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.AutocompleteTag
import com.CannedF0xy.hideout.network.model.E621Response
import com.CannedF0xy.hideout.network.model.Pool
import com.CannedF0xy.hideout.network.model.VoteResponse
import com.CannedF0xy.hideout.vpn.VpnBlockedException
import com.CannedF0xy.hideout.vpn.VpnGuard
import java.util.concurrent.TimeUnit
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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
    suspend fun addFavorite(@Field("post_id") postId: Int): retrofit2.Response<Unit>

    @DELETE("favorites/{id}.json")
    suspend fun removeFavorite(@Path("id") postId: Int): retrofit2.Response<Unit>

    @POST("posts/{id}/votes.json")
    @FormUrlEncoded
    suspend fun votePost(
        @Path("id") postId: Int,
        @Field("score") score: Int,
        @Field("no_unvote") noUnvote: Boolean = false
    ): retrofit2.Response<VoteResponse>

    @GET("pools/{id}.json")
    suspend fun getPool(@Path("id") poolId: Int): Pool

    @GET("users.json")
    suspend fun searchUsers(
        @Query("search[name_matches]") name: String,
        @Query("limit") limit: Int = 1
    ): ResponseBody

    @GET("users/{id}.json")
    suspend fun getUser(@Path("id") userId: Int): ResponseBody

    @POST("iqdb_queries.json")
    @FormUrlEncoded
    suspend fun iqdbByPost(@Field("search[post_id]") postId: Int): ResponseBody

    @POST("iqdb_queries.json")
    @FormUrlEncoded
    suspend fun iqdbByUrl(@Field("search[url]") url: String): ResponseBody

    @GET("tags.json")
    suspend fun searchTags(
        @Query("search[name]") name: String,
        @Query("limit") limit: Int = 1
    ): ResponseBody

    @GET("wiki_pages.json")
    suspend fun searchWiki(
        @Query("search[title]") title: String,
        @Query("limit") limit: Int = 1
    ): ResponseBody
}

object NetworkModule {
    private const val BASE_URL = "https://e621.net/"
    private const val API_HOST = "e621.net"
    private const val MAX_RATE_LIMIT_RETRIES = 2
    private const val RATE_LIMIT_BACKOFF_MS = 1000L
    var api: E621ApiService? = null

    const val DEFAULT_USER_AGENT = "HideoutApp/1.0 (by CannedF0xy on e621)"

    var username: String = ""
    var apiKey: String = ""
    var cfClearance: String = ""

    var onCloudflareChallenge: (() -> Unit)? = null

    private val headerInterceptor = Interceptor { chain ->
        if (VpnGuard.shouldBlock()) throw VpnBlockedException()
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

        val request = requestBuilder.build()
        val isApiHost = request.url.host == API_HOST

        var attempt = 0
        var response: Response
        while (true) {
            if (isApiHost) RequestThrottle.awaitTurn()
            response = chain.proceed(request)
            if (response.code != 503 || isCloudflareChallenge(response) || attempt >= MAX_RATE_LIMIT_RETRIES) break
            response.close()
            attempt++
            Thread.sleep(RATE_LIMIT_BACKOFF_MS * attempt)
        }

        if (response.code == 403 || (response.code == 503 && isCloudflareChallenge(response))) {
            onCloudflareChallenge?.invoke()
        }

        response
    }

    private fun isCloudflareChallenge(response: Response): Boolean {
        if (response.header("cf-mitigated").equals("challenge", ignoreCase = true)) return true
        val server = response.header("Server") ?: ""
        if (!server.contains("cloudflare", ignoreCase = true)) return false
        val bodyText = try { response.peekBody(16384).string() } catch (e: Exception) { "" }
        return bodyText.contains("Just a moment", ignoreCase = true) ||
            bodyText.contains("challenge-platform", ignoreCase = true) ||
            bodyText.contains("cf-chl", ignoreCase = true)
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
