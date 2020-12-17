package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup

class FeedRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    suspend fun getFeeds(): Flow<List<DomainFeedChannelItem>> {
    //fun getFeeds(): Flow<List<FeedChannelItem>> = flow {

        /*
        if (localDataSource.isEmpty()) {
            val feedChannelItem = remoteDataSource.getFeedChannelItems()
            localDataSource.saveFeedChannelItems(feedChannelItem)
        }

         */

        /**
         * Devolver los datos gardados en BBDD
         */
        return localDataSource.getFeedChannelItems()
        //return remoteDataSource.getFeedChannelItems()
        //emit(localDataSource.getFeedChannelItems())
    }

    suspend fun checkNetworkFeeds(): RssResponse<ServerFeed> {
        /**
         * Buscar los feeds en la red
         */
        val newFeeds = withTimeout(15_000) { remoteDataSource.getFeedInfo() }

        return newFeeds
    }

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
    suspend fun getFeeds(): Flow<List<DomainFeed>>
    suspend fun getFeedIdByLink(link: String): Long

    /**
     * FeedChannel
     */
    //suspend fun feedChannelIsEmpty(): Boolean
    //suspend fun feedChannelSize(): Int
    suspend fun saveFeedChannel(feedChannel: DomainFeedChannel): Long
    suspend fun getFeedChannel(feedId: Int): Flow<DomainFeedChannel>
    suspend fun getFeedChannelIdByFeedId(feedId: Long): Long

    /**
     * FeedChannelItem
     */
    suspend fun feedChannelItemsIsEmpty(): Boolean
    suspend fun feedChannelItemsSize(): Int
    suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>)
    suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>>

}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainFeedChannelItem>
    suspend fun getFeedInfo(): RssResponse<ServerFeed>

}