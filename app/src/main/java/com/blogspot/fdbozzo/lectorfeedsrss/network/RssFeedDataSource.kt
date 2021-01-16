package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.RemoteDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceEndingWithChar
import com.blogspot.fdbozzo.lectorfeedsrss.util.getSrcImage
import retrofit2.HttpException
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed

class RssFeedDataSource(): RemoteDataSource {
    //lateinit var listDomainFeedChannelItem: List<DomainFeedChannelItem>

    //override suspend fun getFeedChannelItems(): List<DomainFeedChannelItem> {
    override suspend fun getFeedsFromUrl(apiBaseUrl: String): RssResponse<ServerFeed> {

        try {
            /*
            var valApiBaseUrl = apiBaseUrl

            // Comprobar si la URL tiene la barra final
            if (!apiBaseUrl.endsWith("/")) {
                valApiBaseUrl = "$apiBaseUrl/"
            }
             */
            val valApiBaseUrl = apiBaseUrl.forceEndingWithChar("/")

            Timber.d("[Timber] valApiBaseUrl = ${valApiBaseUrl}")
            //val response = RssApi.retrofitService.getRss()
            val response = rssApi(valApiBaseUrl).getRss()

            //withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let {

                        val feed = it
                        val feedChannel = feed.channel
                        val feedChannelItem = feedChannel.channelItems

                        // Completo algunos datos faltantes del Feed con info del FeedChannel
                        feed.linkName = feedChannel.title

                        if (feedChannel.links.isNotEmpty()) {
                            if (feedChannel.links.size > 1 && feedChannel.links[1].text.isNotEmpty()) {
                                feedChannel.link = feedChannel.links[1].text
                            } else {
                                feedChannel.link = feedChannel.links[0].text
                            }
                            feed.link = feedChannel.link
                        }


                        if (feedChannelItem != null) {
                            Timber.d(feedChannelItem.size.toString())

                            for (i in feedChannelItem.indices) {
                                /*
                                Timber.d("[Timber] index %d %s", i, feedChannelItem[i].title)
                                Timber.d("[Timber] index %d %s", i, feedChannelItem[i].link)
                                Timber.d("[Timber] index %d %s", i, feedChannelItem[i].pubDate)
                                //Timber.d("[Timber] index %d %s", i, articles[i].description)
                                //Timber.d("[Timber] index %d %s", i, articles[i].guid)
                                 */

                                /**
                                 * Obtener un link de imagen para mostrar en el item.
                                 * Puede haber una en "description", en "contentEncoded" o ninguna.
                                 */

                                var imagen = ""

                                if (imagen.isEmpty() && feedChannelItem[i].description.isNotEmpty()) {
                                    // Obtengo la URL de la imagen de la descripción (si hay una)
                                    imagen = getSrcImage(feedChannelItem[i].description)
                                }

                                if (imagen.isEmpty() && feedChannelItem[i].contentEncoded.isNotEmpty()) {
                                    imagen = getSrcImage(feedChannelItem[i].contentEncoded)
                                }

                                if (imagen.isNotEmpty()) {
                                    feedChannelItem[i].imageLink = imagen
                                }
                            }

                        }

                        return RssResponse.Success(it)
                    }
                } else {
                    Timber.d("[Timber] Error network operation failed with ${response.code()}")
                    //_status.value = RssApiStatus.ERROR
                    //return RssResponse.Error(Exception(RssApiStatus.ERROR.toString()))
                    //return RssResponse.Error(Exception("Error code: ${response.code()}, message: ${response.message()}"))
                    return RssResponse.Error(Exception("Error: $response"))
                }
            //}

        } catch (e: HttpException) {
            Timber.d("[Timber] Exception ${e.message}")
            //_status.value = RssApiStatus.ERROR
            return RssResponse.Error(Exception(e.message()))

        } catch (e: Throwable) {
            Timber.d("[Timber] Ooops: Something else went wrong")
            //_status.value = RssApiStatus.ERROR
            return RssResponse.Error(Exception(e.message))
        }
        //return ServerFeed()
        return RssResponse.Error(Exception("Error desconocido"))
    }
}