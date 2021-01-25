package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class RetrofitFactory {


    companion object {

        private var _BASE_URL = "https://localhost/"
        val BASE_URL
            get() = _BASE_URL

        private lateinit var retrofit: Retrofit
        //var builder: Retrofit.Builder()

        private var builder: Retrofit.Builder = Retrofit.Builder()
            //.addConverterFactory(SimpleXmlConverterFactory.create())
            .baseUrl(BASE_URL)

        @Volatile
        private var rssApiService: RssApiService? = null


        fun getRssApiService(apiBaseUrl: String): RssApiService {

            synchronized(this) {

                //var instance = INSTANCE
                var instance = rssApiService

                // Si se indica la misma URL, devolver el objeto existente
                if (instance != null && BASE_URL.isNotEmpty() && BASE_URL == apiBaseUrl && rssApiService != null) {
                    Timber.d("[Timber] getRssApiService(${apiBaseUrl}) --> MISMA INSTANCIA. La URL ya estaba cargada. Devuelve mismo servicio sin crear otro.")
                    return instance as RssApiService
                }

                //INSTANCE = instance
                Timber.d("[Timber] getRssApiService(${apiBaseUrl}) --> Nuevo servicio creado.")
                if (BASE_URL.isEmpty())
                    throw Exception("apiBaseUrl está vacía!")

                changeApiBaseUrl(apiBaseUrl)
                instance = builder.build().create(RssApiService::class.java)
                rssApiService = instance
                return instance
            }
        }

        private fun makeOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                //.addInterceptor(getHeaderInterceptor())
                .addInterceptor(makeLoggingInterceptor())
                .build()
        }

        private fun makeLoggingInterceptor(): HttpLoggingInterceptor {
            val logging = HttpLoggingInterceptor()
            //logging.level = HttpLoggingInterceptor.Level.BODY
            logging.level = HttpLoggingInterceptor.Level.BODY
            return logging
        }

        private fun changeApiBaseUrl(newApiBaseUrl: String) {
            _BASE_URL = newApiBaseUrl

            builder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(makeOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
            //.build() //.create(RssApiService::class.java)
        }

        fun getHeaderInterceptor(): Interceptor {
            return object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request().newBuilder()
                            .header("Cache-Control", "no-cache")
                            .header("Connection", "keep-alive")
                            .header("User-Agent", "OkHttpClient/3")
                            .header("Accept-Encoding", "*/*")
                            .build()
                    return chain.proceed(request)
                }
            }
        }

        /*
        fun <S> createService(serviceClass: Class<S>?): S {
            return createService(serviceClass)
        }
         */


    }

    /*
    var builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(makeOkHttpClient())
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build().create(RssApiService::class.java)
     */

    /*
    fun makeRetrofitService(): RssApiService {
        Timber.d("[Timber] makeRetrofitService(${BASE_URL})")
        if (BASE_URL.isEmpty())
            throw Exception("apiBaseUrl está vacía!")

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(makeOkHttpClient())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build().create(RssApiService::class.java)
    }
     */

    /*
    fun <S> createService(serviceClass: Class<S>?, token: AccessToken): S {
        val authToken: String = token.getTokenType().concat(token.getAccessToken())
        return createService(serviceClass, authToken)
    }
     */
}