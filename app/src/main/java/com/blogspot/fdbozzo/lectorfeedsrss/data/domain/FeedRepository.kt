package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupWithFeeds
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannel as ServerFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannelItem as ServerFeedChannelItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import java.util.HashMap
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup

class FeedRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Devolver los feeds guardados en BBDD
     */
    fun getFeeds(): Flow<List<DomainFeedChannelItemWithFeed>> = localDataSource.getFeedChannelItemsWithFeed()

    /**
     * Buscar los feeds en la red
     */
    suspend fun checkNetworkFeeds(apiBaseUrl: String): RssResponse<ServerFeed> {

        val rssApiResponse = withTimeout(15_000) {
            remoteDataSource.getFeeds(apiBaseUrl)
        }

        when (rssApiResponse) {
            is RssResponse.Success -> {

                val serverFeed = rssApiResponse.data

                /**
                 * Guardar feeds en Room
                 */
                saveNetworkFeeds(serverFeed)

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

    private suspend fun saveNetworkFeeds(serverFeed: ServerFeed): Unit {
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


        // Reemplazar el feedId del channel por el id del Feed, antes de guardarlo
        serverFeed.channel.feedId = feedId

        /**
         * Se intenta insertar el FeedChannel, pero si ya existe devolverá -1 y se recuperará su id actual
         */
        var feedChannelId = localDataSource.saveFeedChannelFromServer(serverFeed.channel)

        if (feedChannelId == -1L)
            feedChannelId = localDataSource.getFeedChannelIdByFeedId(feedId)

        /**
         * Se intenta insertar el FeedChannelItem, pero si ya existe se ignorará
         */
        val listFeedChannelItem = serverFeed.channel.channelItems

        if (listFeedChannelItem != null && listFeedChannelItem.isNotEmpty()) {
            // Reemplazar el feedChannelId del item por el id del Channel, antes de guardarlo
            for (domainFeedChannelItem in listFeedChannelItem) {
                domainFeedChannelItem.feedId = feedChannelId
            }

            localDataSource.saveFeedChannelItemsFromServer(listFeedChannelItem)
        }
    }

    suspend fun deleteAllLocalGroups(): Int {
        return localDataSource.deleteAll()
    }

    suspend fun saveLocalGroup(group: DomainGroup): Long {
        return localDataSource.saveGroup(group)
    }

    suspend fun saveLocalFeed(feed: DomainFeed): Long {
        return localDataSource.saveFeed(feed)
    }

    suspend fun saveLocalFeedChannel(feedChannel: DomainFeedChannel): Long {
        return localDataSource.saveFeedChannel(feedChannel)
    }

    suspend fun getGroupById(key: Long): DomainGroup {
        return localDataSource.getGroupById(key)
    }

    suspend fun getGroupIdByName(name: String): Long {
        return localDataSource.getGroupIdByName(name)
    }

    suspend fun getGroupsWithFeeds(): HashMap<String, List<String>> {
        return localDataSource.getGroupsWithFeeds()
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
    suspend fun getGroupIdByName(name: String): Long
    suspend fun getGroupById(key: Long): DomainGroup
    suspend fun getGroups(): Flow<List<DomainGroup>>
    fun delete(group: Group): Int
    suspend fun deleteAll(): Int
    suspend fun getGroupsWithFeeds(): HashMap<String, List<String>>

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
    fun getFeedChannelItemsWithFeed(): Flow<List<DomainFeedChannelItemWithFeed>>

}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainFeedChannelItem>
    suspend fun getFeeds(apiBaseUrl: String): RssResponse<ServerFeed>

}