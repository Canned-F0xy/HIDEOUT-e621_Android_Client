package com.CannedF0xy.hideout.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FavoriteRepository {
    suspend fun setFavorite(postId: Int, favorite: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val api = NetworkModule.api!!
            if (favorite) {
                val response = api.addFavorite(postId)
                response.isSuccessful || response.code() == 422
            } else {
                val response = api.removeFavorite(postId)
                response.isSuccessful || response.code() == 404
            }
        } catch (e: Exception) {
            false
        }
    }
}
