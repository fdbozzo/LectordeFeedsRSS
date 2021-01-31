package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.RemoteDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceEndingWithChar
import com.blogspot.fdbozzo.lectorfeedsrss.util.getSrcImage
import retrofit2.HttpException
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed

class RssFeedDataSource : RemoteDataSource {

    override suspend fun getFeedsFromUrl(apiBaseUrl: String): RssResponse<ServerFeed> {

        try {
            val valApiBaseUrl = apiBaseUrl.forceEndingWithChar("/")

            Timber.d("[Timber] valApiBaseUrl = $valApiBaseUrl")
            val response = rssApi(valApiBaseUrl).getRss()

            if (response.isSuccessful) {
                response.body()?.let {

                    val feed = it
                    val channel = feed.channel
                    val Item = channel.items

                    // Completo algunos datos faltantes del Feed con info del Channel
                    feed.linkName = channel.title

                    if (channel.links.isNotEmpty()) {
                        if (channel.links.size > 1 && channel.links[1].text.isNotEmpty()) {
                            channel.link = channel.links[1].text
                        } else {
                            channel.link = channel.links[0].text
                        }
                        feed.link = channel.link
                    }


                    if (Item != null) {
                        Timber.d(Item.size.toString())

                        for (i in Item.indices) {
                            /*
                            Timber.d("[Timber] index %d %s", i, Item[i].title)
                            Timber.d("[Timber] index %d %s", i, Item[i].link)
                            Timber.d("[Timber] index %d %s", i, Item[i].pubDate)
                            //Timber.d("[Timber] index %d %s", i, articles[i].description)
                            //Timber.d("[Timber] index %d %s", i, articles[i].guid)
                             */

                            /**
                             * Obtener un link de imagen para mostrar en el item.
                             * Puede haber una en "description", en "contentEncoded" o ninguna.
                             */

                            var imagen = ""

                            if (imagen.isEmpty() && Item[i].description.isNotEmpty()) {
                                // Obtengo la URL de la imagen de la descripción (si hay una)
                                imagen = getSrcImage(Item[i].description)
                            }

                            if (imagen.isEmpty() && Item[i].contentEncoded.isNotEmpty()) {
                                imagen = getSrcImage(Item[i].contentEncoded)
                            }

                            if (imagen.isNotEmpty()) {
                                Item[i].imageLink = imagen
                            }
                        }

                    }

                    return RssResponse.Success(it)
                }
            } else {
                Timber.d("[Timber] Error network operation failed with ${response.code()}")
                return RssResponse.Error(Exception("Error [NWO]: $response"))
            }

        } catch (e: HttpException) {
            println("[Timber] Exception ${e.message}")
            return RssResponse.Error(Exception("HttpException: ${e.message()}"))

        } catch (e: Throwable) {
            println("[Timber] Ooops: Something else went wrong")
            return RssResponse.Error(Exception("Oops: ${e.message}"))
        }
        println("[Timber] Error desconocido")
        return RssResponse.Error(Exception("Error desconocido"))
    }
}