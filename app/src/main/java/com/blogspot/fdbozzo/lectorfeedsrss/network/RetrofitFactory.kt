package com.blogspot.fdbozzo.lectorfeedsrss.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

const val API_BASE_URL = "https://hardzone.es/"

object RetrofitFactory {
    //const val BASE_URL = "https://jsonplaceholder.typicode.com"

    fun makeRetrofitService(): RssApiService {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(makeOkHttpClient())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build().create(RssApiService::class.java)
    }

    private fun makeOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(makeLoggingInterceptor())
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private fun makeLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }


}