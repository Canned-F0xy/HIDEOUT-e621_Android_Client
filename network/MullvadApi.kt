package com.CannedF0xy.hideout.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class MullvadRelay(
    val hostname: String,
    val country_code: String,
    val active: Boolean,
    val ipv4_addr_in: String,
    val pubkey: String
)

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
