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
        localDataSource.saveFeed(domainFeed)
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

    /**
     * FeedChannel
     */
    //suspend fun feedChannelIsEmpty(): Boolean
    //suspend fun feedChannelSize(): Int
    //suspend fun saveFeedChannels(feedChannels: List<DomainFeedChannel>)
    suspend fun getFeedChannel(feedId: Int): Flow<DomainFeedChannel>

    /**
     * FeedChannelItem
     */
    suspend fun feedChannelItemsIsEmpty(): Boolean
    suspend fun feedChannelItemsSize(): Int
    //suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>)
    suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>>

}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainFeedChannelItem>
    suspend fun getFeedInfo(): RssResponse<ServerFeed>

}