package com.CannedF0xy.hideout.network

import com.CannedF0xy.hideout.network.model.VoteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VoteRepository {
    suspend fun vote(postId: Int, score: Int, noUnvote: Boolean = false): VoteResponse? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkModule.api!!.votePost(postId, score, noUnvote)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
