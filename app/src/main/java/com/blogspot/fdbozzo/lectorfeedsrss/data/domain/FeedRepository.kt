package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.toDomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.toDomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannel as ServerFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannelItem as ServerFeedChannelItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup

class FeedRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Devolver los feeds guardados en BBDD
     */
    fun getFeeds(): Flow<List<DomainFeedChannelItem>> = localDataSource.getFeedChannelItems2()

    /**
     * Buscar los feeds en la red
     */
    suspend fun checkNetworkFeeds(): RssResponse<ServerFeed> {
        val rssApiResponse = withTimeout(15_000) {
            remoteDataSource.getFeedInfo()
        }

        when (rssApiResponse) {
            is RssResponse.Success -> {

                val serverFeed = (rssApiResponse as RssResponse.Success<ServerFeed>).data

                /**
                 * Guardar feeds en Room
                 */
                saveNetworkFeedsToBBDD(serverFeed)

                /**
                 * Filtrar feeds leidos
                 */

            }
            is RssResponse.Error -> {
                // No se trata aquí, sino en el ViewModel
            }
        }

        return rssApiResponse
    }

    suspend fun saveNetworkFeedsToBBDD(serverFeed: ServerFeed): Unit {
        /**
         * Guardar los feeds en BBDD
         */
        if (localDataSource.groupIsEmpty()) {
            localDataSource.saveGroup(DomainGroup())
        }

        /**
         * Se intenta insertar el Feed, pero si ya existe devolverá -1 y se recuperará su id actual
         */
        var feedId = localDataSource.saveFeedFromServer(serverFeed)

        if (feedId == -1L)
            feedId = localDataSource.getFeedIdByLink(serverFeed.link)


        // Completar algunos datos del Feed con los del channel
        serverFeed.linkName = serverFeed.channel.title
        serverFeed.link = serverFeed.channel.link

        // Reemplazar el feedId del channel por el id del Feed
        serverFeed.channel.feedId = feedId

        /**
         * Se intenta insertar el FeedChannel, pero si ya existe devolverá -1 y se recuperará su id actual
         */
        var feedChannelId = localDataSource.saveFeedChannelFromServer(serverFeed.channel)

        if (feedChannelId == -1L)
            feedChannelId = localDataSource.getFeedChannelIdByFeedId(feedId)

        for (domainFeedChannelItem in serverFeed.channel.channelItems!!) {
            domainFeedChannelItem.feedId = feedChannelId
        }

        val listFeedChannelItem = serverFeed.channel.channelItems

        if (listFeedChannelItem != null)
            localDataSource.saveFeedChannelItemsFromServer(listFeedChannelItem)
    }

    /*
    suspend fun saveNetworkFeeds(domainFeed: DomainFeed): Unit {
        /**
         * Guardar los feeds en BBDD
         */
        if (localDataSource.groupIsEmpty()) {
            localDataSource.saveGroup(DomainGroup())
        }

        /**
         * Se intenta insertar el Feed, pero si ya existe devolverá -1 y se recuperará su id actual
         */
        var feedId = localDataSource.saveFeed(domainFeed)

        if (feedId == -1L)
            feedId = localDataSource.getFeedIdByLink(domainFeed.link)


        // Completar algunos datos del Feed con los del channel
        domainFeed.linkName = domainFeed.channel.title
        domainFeed.link = domainFeed.channel.link

        // Reemplazar el feedId del channel por el id del Feed
        domainFeed.channel.feedId = feedId

        /**
         * Se intenta insertar el FeedChannel, pero si ya existe devolverá -1 y se recuperará su id actual
         */
        var feedChannelId = localDataSource.saveFeedChannel(domainFeed.channel)

        if (feedChannelId == -1L)
            feedChannelId = localDataSource.getFeedChannelIdByFeedId(feedId)

        for (domainFeedChannelItem in domainFeed.channel.channelItems!!) {
            domainFeedChannelItem.feedId = feedChannelId
        }

        val listFeedChannelItem = domainFeed.channel.channelItems

        if (listFeedChannelItem != null)
            localDataSource.saveFeedChannelItems(listFeedChannelItem)
    }
     */

}

interface LocalDataSource {

    /**
     * Group
     */
    suspend fun groupIsEmpty(): Boolean
    suspend fun groupSize(): Int
    suspend fun saveGroup(group: DomainGroup): Long
    suspend fun getGroupWithName(name: String): DomainGroup
    suspend fun getGroupId(name: String): Long
    suspend fun getGroups(): Flow<List<DomainGroup>>

    /**
     * Feed
     */
    suspend fun feedIsEmpty(): Boolean
    suspend fun feedSize(): Int
    suspend fun saveFeed(feed: DomainFeed): Long
    suspend fun saveFeedFromServer(feed: ServerFeed): Long
    suspend fun getFeeds(): Flow<List<DomainFeed>>
    suspend fun getFeedIdByLink(link: String): Long

    /**
     * FeedChannel
     */
    //suspend fun feedChannelIsEmpty(): Boolean
    //suspend fun feedChannelSize(): Int
    suspend fun saveFeedChannel(feedChannel: DomainFeedChannel): Long
    suspend fun saveFeedChannelFromServer(feedChannel: ServerFeedChannel): Long
    suspend fun getFeedChannel(feedId: Int): Flow<DomainFeedChannel>
    suspend fun getFeedChannelIdByFeedId(feedId: Long): Long

    /**
     * FeedChannelItem
     */
    suspend fun feedChannelItemsIsEmpty(): Boolean
    suspend fun feedChannelItemsSize(): Int
    suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>)
    suspend fun saveFeedChannelItemsFromServer(feedChannelItems: List<ServerFeedChannelItem>)
    suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>>
    fun getFeedChannelItems2(): Flow<List<DomainFeedChannelItem>>

}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainFeedChannelItem>
    suspend fun getFeedInfo(): RssResponse<ServerFeed>

}