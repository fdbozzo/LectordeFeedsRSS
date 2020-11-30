package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
import retrofit2.Response
import retrofit2.http.GET

interface RssApiService {
    @GET("/feed")
    //fun feed(): Call<Rss>
    suspend fun getRss(): Response<Feed>
    //suspend fun getRss(): List<Feed>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object RssApi {
    val retrofitService : RssApiService by lazy { RetrofitFactory.makeRetrofitService() }
}
