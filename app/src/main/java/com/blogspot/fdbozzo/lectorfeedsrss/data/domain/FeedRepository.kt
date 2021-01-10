package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.toRoomGroup
import kotlinx.coroutines.Dispatchers
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannel as ServerFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannelItem as ServerFeedChannelItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
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
    fun getFilteredFeeds(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainFeedChannelItemWithFeed>> =
        localDataSource.getFeedChannelItemsWithFeed(
            selectedFeedOptions
        )

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
                Timber.d("[Timber] FeedRepository.checkNetworkFeeds() - Guardar las ${serverFeed.channel.channelItems?.size ?: 0} noticias de ${apiBaseUrl}")

                try {
                    saveNetworkFeeds(serverFeed)
                } catch (e: Exception) {
                    Timber.d(e, "[Timber] FeedRepository.checkNetworkFeeds() - ERROR")
                }

            }
            is RssResponse.Error -> {
                // No se trata aquí, sino en el ViewModel
            }
        }

        return rssApiResponse
    }

    private suspend fun saveNetworkFeeds(serverFeed: ServerFeed): Unit {
        var groupId: Long? = null
        var feeds = 0L
        var feedId = 0L
        var feedChannels = 0L
        var feedChannelId = 0L

        println("[Timber] 1")

        /**
         * Guardar los feeds en BBDD
         */
        try {
            if (withContext(Dispatchers.IO) {localDataSource.groupIsEmpty()}) {
                println("[Timber] 2")
                withContext(Dispatchers.IO) {localDataSource.saveGroup(DomainGroup())}
                println("[Timber] 3")
            }
            groupId = withContext(Dispatchers.IO) {localDataSource.getGroupIdByName(Group.DEFAULT_NAME)}
            println("[Timber] 4 FeedRepository.saveNetworkFeeds() - GroupId=$groupId")
            Timber.d("[Timber] FeedRepository.saveNetworkFeeds() - GroupId=%d", groupId)

            if (groupId == null)
                throw Exception("localDataSource.getGroupIdByName(${Group.DEFAULT_NAME}) -> Nuevo groupId = null")

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - Group")
        }

        /**
         * Se intenta insertar el Feed y recuperar su id, pero si ya existe devolverá -1
         */
        try {
            feeds = withContext(Dispatchers.IO) {localDataSource.saveFeedFromServer(serverFeed.also {
                it.groupId = groupId?: 0
            })}
            println("[Timber] 5")
            feedId = withContext(Dispatchers.IO) {localDataSource.getFeedIdByLink(serverFeed.link)}
            println("[Timber] 6 FeedRepository.saveNetworkFeeds(${serverFeed.linkName}): FEED - FeedId=$feedId (count=$feeds)")

            Timber.d(
                "[Timber] FeedRepository.saveNetworkFeeds(%s): FEED - FeedId=%d (count=%d)",
                serverFeed.linkName,
                feedId, feeds
            )

            // Reemplazar el feedId del channel por el id del Feed, antes de guardarlo
            serverFeed.channel.feedId = feedId
            println("[Timber] 7 serverFeed.channel.feedId=${serverFeed.channel.feedId}")

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - Feed")
        }

        try {
            /**
             * Se intenta insertar el FeedChannel, pero si ya existe devolverá -1 y se recuperará su id actual
             */
            feedChannels = withContext(Dispatchers.IO) {localDataSource.saveFeedChannelFromServer(serverFeed.channel)}
            println("[Timber] 8 feedChannels=$feedChannels")
            feedChannelId = withContext(Dispatchers.IO) {localDataSource.getFeedChannelIdByFeedId(feedId)}
            println("[Timber] 9 feedChannelId=$feedChannelId")

            Timber.d(
                "FeedRepository.saveNetworkFeeds(%s): CHANNEL - Id=%d, feedChannels=%d",
                serverFeed.linkName,
                feedChannelId,
                feedChannels
            )

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - FeedChannel")
        }

        try {
            /**
             * Se intenta insertar el FeedChannelItem, pero si ya existe se ignorará
             */
            val listFeedChannelItem = serverFeed.channel.channelItems
            println("[Timber] 10")

            if (listFeedChannelItem != null && listFeedChannelItem.isNotEmpty()) {
                // Reemplazar el feedChannelId del item por el id del Channel, antes de guardarlo
                for (domainFeedChannelItem in listFeedChannelItem) {
                    domainFeedChannelItem.feedId = feedId
                    println("[Timber] 10 domainFeedChannelItem.feedId=${domainFeedChannelItem.feedId}")
                }

                //println("[Timber] 11 listFeedChannelItem=${}")
                withContext(Dispatchers.IO) {localDataSource.saveFeedChannelItemsFromServer(listFeedChannelItem)}
                Timber.d(
                    "FeedRepository.saveNetworkFeeds(%s): ITEMS - Guardados",
                    serverFeed.linkName
                )

            }
        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - FeedChannelItem")
        }
    }

    /**
     * GROUP
     */
    suspend fun deleteAllLocalGroups(): Int {
        return withContext(Dispatchers.IO) {localDataSource.deleteAllGroups()}
    }

    suspend fun saveLocalGroup(group: DomainGroup): Long {
        return withContext(Dispatchers.IO) {localDataSource.saveGroup(group)}
    }

    suspend fun updateGroup(group: DomainGroup): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateGroup(group)}
    }

    suspend fun getGroupById(key: Long): DomainGroup? {
        return withContext(Dispatchers.IO) {localDataSource.getGroupById(key)}
    }

    suspend fun getGroupIdByName(name: String): Long? {
        return withContext(Dispatchers.IO) {localDataSource.getGroupIdByName(name)}
    }

    suspend fun getGroupByName(groupName: String): DomainGroup? {
        return withContext(Dispatchers.IO) {localDataSource.getGroupByName(groupName)}
    }

    suspend fun deleteGroupByName(groupName: String): Int {
        val group = withContext(Dispatchers.IO) {localDataSource.getGroupByName(groupName)}
        Timber.d("FeedRepository.deleteGroupByName(%s) = %s", groupName, group?.id ?: 0)
        return if (group != null) {
            withContext(Dispatchers.IO) {localDataSource.deleteGroup(group.toRoomGroup())}
        } else {
            0
        }
    }

    /**
     * FEED
     */
    suspend fun saveLocalFeed(feed: DomainFeed): Long {
        return withContext(Dispatchers.IO) {localDataSource.saveFeed(feed)}
    }

    suspend fun saveLocalFeedChannel(feedChannel: DomainFeedChannel): Long {
        return withContext(Dispatchers.IO) {localDataSource.saveFeedChannel(feedChannel)}
    }

    suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateFeedFavoriteState(id, favorite)}
    }

    fun getGroupsWithFeeds(): Flow<HashMap<String, List<String>>> {
        return localDataSource.getGroupsWithFeeds()
    }

    suspend fun getFeedByLinkName(linkName: String): DomainFeed {
        return withContext(Dispatchers.IO) {localDataSource.getFeedWithLinkName(linkName)}
    }

    suspend fun getFeedIdByLink(link: String): Long {
        return withContext(Dispatchers.IO) {localDataSource.getFeedIdByLink(link)}
    }

    suspend fun deleteFeed(feed: DomainFeed): Int {
        return withContext(Dispatchers.IO) {localDataSource.deleteFeed(feed)}
    }

    suspend fun deleteGroup(group: Group): Int {
        return withContext(Dispatchers.IO) {localDataSource.deleteGroup(group)}
    }

    /**
     * FEEDCHANNELITEM
     */
    suspend fun getFeedChannelItemWithFeed(id: Long): DomainFeedChannelItemWithFeed? {
        return withContext(Dispatchers.IO) {localDataSource.getFeedChannelItemWithFeed(id)}
    }

    fun getFeedChannelItemWithFeedFlow(id: Long): Flow<DomainFeedChannelItemWithFeed> =
        localDataSource.getFeedChannelItemWithFeedFlow(id)

    suspend fun updateReadStatus(id: Long, read: Boolean): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateReadStatus(id, read)}
    }

    suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateReadLaterStatus(id, readLater)}
    }

    suspend fun updateInverseReadLaterStatus(id: Long): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateInverseReadLaterStatus(id)}
    }

    suspend fun updateFeedReadStatus(feedId: Long): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateFeedReadStatus(feedId)}
    }

    suspend fun updateGroupFeedReadStatus(gropId: Long): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateGroupFeedReadStatus(gropId)}
    }

    suspend fun updateMarkAllFeedAsRead(): Int {
        return withContext(Dispatchers.IO) {localDataSource.updateMarkAllFeedAsRead()}
    }

}

interface LocalDataSource {

    /**
     * Group
     */
    suspend fun groupIsEmpty(): Boolean
    suspend fun groupSize(): Long
    suspend fun saveGroup(group: DomainGroup): Long
    suspend fun updateGroup(group: DomainGroup): Int
    suspend fun getGroupIdByName(name: String): Long?
    suspend fun getGroupById(key: Long): DomainGroup?
    suspend fun getGroupByName(groupName: String): DomainGroup?
    suspend fun getGroups(): Flow<List<DomainGroup>?>
    suspend fun deleteGroup(group: Group): Int
    suspend fun deleteAllGroups(): Int
    fun getGroupsWithFeeds(): Flow<HashMap<String, List<String>>>

    /**
     * Feed
     */
    suspend fun feedIsEmpty(): Boolean
    suspend fun feedSize(): Int
    suspend fun saveFeed(feed: DomainFeed): Long
    suspend fun saveFeedFromServer(feed: ServerFeed): Long
    suspend fun getFeeds(): Flow<List<DomainFeed>>
    suspend fun getFeedIdByLink(link: String): Long
    suspend fun getFeedWithLinkName(linkName: String): DomainFeed
    suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int
    suspend fun deleteFeed(feed: DomainFeed): Int

    /**
     * FeedChannel
     */
    //suspend fun feedChannelIsEmpty(): Boolean
    //suspend fun feedChannelSize(): Int
    suspend fun saveFeedChannel(feedChannel: DomainFeedChannel): Long
    suspend fun saveFeedChannelFromServer(feedChannel: ServerFeedChannel): Long
    suspend fun getFeedChannel(feedId: Long): Flow<DomainFeedChannel>
    suspend fun getFeedChannelIdByFeedId(feedId: Long): Long

    /**
     * FeedChannelItem
     */
    suspend fun feedChannelItemsIsEmpty(): Boolean
    suspend fun feedChannelItemsSize(): Int
    suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>)
    suspend fun saveFeedChannelItemsFromServer(feedChannelItems: List<ServerFeedChannelItem>)
    suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>>
    suspend fun getFeedChannelItemWithFeed(id: Long): DomainFeedChannelItemWithFeed?
    fun getFeedChannelItemWithFeedFlow(id: Long): Flow<DomainFeedChannelItemWithFeed>
    fun getFeedChannelItemsWithFeed(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainFeedChannelItemWithFeed>>
    suspend fun updateReadStatus(id: Long, read: Boolean): Int
    suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int
    suspend fun updateInverseReadLaterStatus(id: Long): Int
    suspend fun updateFeedReadStatus(feedId: Long): Int
    suspend fun updateGroupFeedReadStatus(gropId: Long): Int
    suspend fun updateMarkAllFeedAsRead(): Int
}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainFeedChannelItem>
    suspend fun getFeeds(apiBaseUrl: String): RssResponse<ServerFeed>

}