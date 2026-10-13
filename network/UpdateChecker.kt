package com.CannedF0xy.hideout.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

data class GithubAsset(val name: String? = null, val browser_download_url: String? = null, val size: Long? = null)

data class GithubRelease(val tag_name: String, val html_url: String, val assets: List<GithubAsset>? = null) {
    fun apkAsset(): GithubAsset? = assets?.firstOrNull { it.name?.endsWith(".apk", ignoreCase = true) == true && it.browser_download_url != null }
}

suspend fun checkGithubUpdate(): GithubRelease? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://api.github.com/repos/Canned-F0xy/HIDEOUT-e621_Android_Client/releases/latest")
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                return@withContext Gson().fromJson(body, GithubRelease::class.java)
            }
        }
    } catch (e: Exception) {}
    return@withContext null
}
