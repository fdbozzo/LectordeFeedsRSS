package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import retrofit2.Response
import retrofit2.http.GET

enum class RssApiStatus { LOADING, ERROR, DONE }

interface RssApiService {

    //@GET("/feed")
    @GET("/feed")
    //fun feed(): Call<Rss>
    suspend fun getRss(): Response<ServerFeed>
    //suspend fun getRss(): List<Feed>
}

fun rssApi(apiBaseUrl: String): RssApiService {
    //val retrofitService : RssApiService by lazy { RetrofitFactory.makeRetrofitService() }
    val retrofitService : RssApiService = RetrofitFactory.getRssApiService(apiBaseUrl)
    return retrofitService
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
/*
object RssApi {
    val retrofitService : RssApiService by lazy { RetrofitFactory.makeRetrofitService() }
}
 */
