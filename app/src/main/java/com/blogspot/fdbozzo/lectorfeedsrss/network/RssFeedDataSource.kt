package com.blogspot.fdbozzo.lectorfeedsrss.network

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.RemoteDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.RssApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

class RssFeedDataSource(): RemoteDataSource {
    lateinit var listDomainFeedChannelItem: List<DomainFeedChannelItem>

    //override suspend fun getFeedChannelItems(): List<DomainFeedChannelItem> {
    override suspend fun getFeeds(): RssResponse<ServerFeed> {
        val response = RssApi.retrofitService.getRss()

        try {
            //withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let {

                        // Actualizar la BBDD
                        /*
                        _items = Transformations.map(database.videoDao.getVideos()) {
                            it.asDomainModel()
                        }

                         */

                        val feed = it
                        val feedChannel = feed.channel
                        val feedChannelItem = feedChannel.channelItems
                        //val articles = it.channel!!.channelItems
                        //Timber.d("feed = $feed")

                        if (feedChannelItem != null) {
                            Timber.d(feedChannelItem.size.toString())
                            for (i in feedChannelItem.indices) {
                                Timber.d("index %d %s", i, feedChannelItem[i].title)
                                Timber.d("index %d %s", i, feedChannelItem[i].link)
                                Timber.d("index %d %s", i, feedChannelItem[i].pubDate)
                                //Timber.d("index %d %s", i, articles[i].description)
                                //Timber.d("index %d %s", i, articles[i].guid)
                            }
                            /** ACTUALIZAR EL ORIGEN DE DATOS (ITEMS) */
                            /** ACTUALIZAR EL ORIGEN DE DATOS (ITEMS) */
                            //initRecyclerView(articles)
                            //items_normales = articles
                            Timber.d("actualizando LiveData...")
                            //_items.value = articles     // Dispara evento de cambio!
                            Timber.d("LiveData actualizado!")
                            //_status.value = RssApiStatus.DONE

                        }

                        return RssResponse.Success(it)
                    }
                } else {
                    Timber.d("Error network operation failed with ${response.code()}")
                    //_status.value = RssApiStatus.ERROR
                    return RssResponse.Error(Exception(RssApiStatus.ERROR.toString()))
                }
            //}

        } catch (e: HttpException) {
            Timber.d("Exception ${e.message}")
            //_status.value = RssApiStatus.ERROR
            return RssResponse.Error(Exception(e.message()))

        } catch (e: Throwable) {
            Timber.d("Ooops: Something else went wrong")
            //_status.value = RssApiStatus.ERROR
            return RssResponse.Error(Exception(e.message))
        }
        //return ServerFeed()
        return RssResponse.Error(Exception("Error desconocido"))
    }
}