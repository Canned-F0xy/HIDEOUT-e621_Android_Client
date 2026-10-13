package com.CannedF0xy.hideout.data

import androidx.compose.runtime.*
import com.CannedF0xy.hideout.network.model.Post

object LocalPostManager {
    val favoriteStates = mutableStateMapOf<Int, Boolean>()
    val scoreStates = mutableStateMapOf<Int, Int>()
    val voteStates = mutableStateMapOf<Int, Int>()

    fun getVote(post: Post): Int = voteStates[post.id] ?: if (isFavorited(post)) 1 else 0

    fun markFavorite(post: Post, fav: Boolean) {
        favoriteStates[post.id] = fav
    }

    fun applyVoteResult(post: Post, score: Int?, ourScore: Int?) {
        if (score != null) scoreStates[post.id] = score
        voteStates[post.id] = ourScore ?: 0
    }

    fun isFavorited(post: Post): Boolean {
        return favoriteStates[post.id] ?: (post.is_favorited ?: false)
    }

    fun getScore(post: Post): Int {
        return scoreStates[post.id] ?: (post.score?.total ?: 0)
    }

    fun toggleFavorite(post: Post) {
        val currentFav = isFavorited(post)
        val currentScore = getScore(post)
        favoriteStates[post.id] = !currentFav
        scoreStates[post.id] = currentScore + (if (currentFav) -1 else 1)
    }

    fun setFavorite(post: Post, fav: Boolean) {
        val currentFav = isFavorited(post)
        if (currentFav != fav) {
            favoriteStates[post.id] = fav
            scoreStates[post.id] = getScore(post) + (if (fav) 1 else -1)
        }
    }
}
