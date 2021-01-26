package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

enum class RssApiStatus { LOADING, ERROR, DONE }

interface RssApiService {

    @Headers(
        //"Cache-Control: no-cache",
        //"Connection: keep-alive",
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        //"Accept-Encoding: gzip, deflate, br",  // Da problemas con los XML
        //"Content-Type: text/xml",
        //"Accept-Charset: utf-8"
        //"Accept-Language: es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"
        "User-Agent: LectorDeFeedsRSS/1.0"
    )
    @GET("/feed")
    //fun feed(): Call<Rss>
    suspend fun getRss(): Response<ServerFeed>
}

fun rssApi(apiBaseUrl: String): RssApiService {
    val retrofitService : RssApiService = RetrofitFactory.getRssApiService(apiBaseUrl)
    return retrofitService
}
