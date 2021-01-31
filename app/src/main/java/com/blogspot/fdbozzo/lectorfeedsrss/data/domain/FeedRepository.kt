package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.toRoomGroup
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Channel as ServerChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Item as ServerItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.HashMap
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Channel as DomainChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.ItemWithFeed as DomainItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup

class FeedRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Devolver los feeds con los items guardados en BBDD
     */
    fun getFilteredFeeds(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainItemWithFeed>> =
        localDataSource.getItemsWithFeed(
            selectedFeedOptions
        )

    /**
     * Buscar los feeds en la red y guardarlos
     */
    suspend fun checkNetworkFeeds(apiBaseUrl: String, groupId: Long? = null): RssResponse<ServerFeed> {
        try {
            /**
             * Buscar los feeds en la red
             */
            val rssApiResponse = getNetworkFeeds(apiBaseUrl)

            when (rssApiResponse) {
                is RssResponse.Success -> {

                    val serverFeed = rssApiResponse.data

                    /**
                     * Guardar feeds en Room
                     */
                    Timber.d("[Timber] FeedRepository.checkNetworkFeeds() - Guardar las ${serverFeed.channel.items?.size ?: 0} noticias de $apiBaseUrl")

                    saveNetworkFeeds(serverFeed, groupId)
                }
                is RssResponse.Error -> {
                    // No se trata aquí, sino en el ViewModel
                }
            }
            return rssApiResponse

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.checkNetworkFeeds --> Error")
            throw e
        }
    }

    /**
     * Buscar los feeds en la red y devuelve la respuesta
     */
    suspend fun getNetworkFeeds(apiBaseUrl: String): RssResponse<ServerFeed> {

        return withTimeout(15_000) {
            remoteDataSource.getFeedsFromUrl(apiBaseUrl)
        }
    }

    private suspend fun saveNetworkFeeds(serverFeed: ServerFeed, parGroupId: Long? = null): Unit {
        var groupId: Long? = parGroupId
        var feeds = 0L
        var feedId: Long? = null
        var channels = 0L
        var channelId = 0L

        println("[Timber] 1")

        /**
         * Si no hay grupos guardados (es nueva instalación), se carga el primero
         * por defecto ("Uncategorized")
         */
        try {
            // Chequea grupos por las dudas. Siempre debe existir uno por defecto.
            if (localDataSource.groupIsEmpty()) {
                println("[Timber] 2")
                localDataSource.saveGroup(DomainGroup())
                println("[Timber] 3")
            }

            // Si groupId == null, entonces recupera el groupId del grupo por defecto (Uncategorized)
            if (groupId == null) {
                groupId = localDataSource.getGroupIdByName(Group.DEFAULT_NAME)
            }
            println("[Timber] 4 FeedRepository.saveNetworkFeeds() - GroupId=$groupId")
            Timber.d("[Timber] FeedRepository.saveNetworkFeeds() - GroupId=%d", groupId)
            /*
            if (groupId == null)
                throw Exception("localDataSource.getGroupIdByName(${Group.DEFAULT_NAME}) -> Nuevo groupId = null")
             */

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - Group")
        }


        /**
         * Se intenta insertar el Feed y recuperar su id, pero si ya existe devolverá -1
         * Antes de llegar a este punto, ya hay grupos y feeds existentes, aquí sólo se recuperan
         * sus noticias.
         */
        try {
            feeds = localDataSource.saveFeedFromServer(serverFeed.also {
                if (groupId != null) {
                    it.groupId = groupId
                }
            })

            println("[Timber] 5 serverFeed.linkName = '${serverFeed.linkName}', serverFeed.channel.title = '${serverFeed.channel.title}'")
            feedId = localDataSource.getFeedIdByLink(serverFeed.link) ?: throw Exception("feedId es null")
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
            throw e
        }

        /**
         * Guardar los feeds en BBDD
         */
        try {
            /**
             * Se intenta insertar el Channel, pero si ya existe devolverá -1 y se recuperará su id actual
             */
            channels = localDataSource.saveChannelFromServer(serverFeed.channel)
            println("[Timber] 8 channels=$channels")
            channelId = localDataSource.getChannelIdByFeedId(feedId)
            println("[Timber] 9 channelId=$channelId")

            Timber.d(
                "FeedRepository.saveNetworkFeeds(%s): CHANNEL - Id=%d, channels=%d",
                serverFeed.linkName,
                channelId,
                channels
            )

        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - Channel")
            throw e
        }

        try {
            /**
             * Se intenta insertar el Item, pero si ya existe se ignorará
             */
            val listItem = serverFeed.channel.items
            println("[Timber] 10")

            if (listItem != null && listItem.isNotEmpty()) {
                // Reemplazar el channelId del item por el id del Channel, antes de guardarlo
                for (domainItem in listItem) {
                    domainItem.feedId = feedId
                    println("[Timber] 10 domainItem.feedId=${domainItem.feedId}")
                }

                //println("[Timber] 11 listItem=${}")
                localDataSource.saveItemsFromServer(listItem)
                Timber.d(
                    "FeedRepository.saveNetworkFeeds(%s): ITEMS - Guardados",
                    serverFeed.linkName
                )

            }
        } catch (e: Exception) {
            Timber.d(e, "[Timber] FeedRepository.saveNetworkFeeds() - ERROR - Item")
            throw e
        }
    }

    /**
     * GROUP
     */
    suspend fun deleteAllLocalGroups(): Int {
        return localDataSource.deleteAllGroups()
    }

    suspend fun saveLocalGroup(group: DomainGroup): Long {
        return localDataSource.saveGroup(group)
    }

    suspend fun updateGroup(group: DomainGroup): Int {
        return localDataSource.updateGroup(group)
    }

    suspend fun getGroupById(key: Long): DomainGroup? {
        return localDataSource.getGroupById(key)
    }

    suspend fun getGroupIdByName(name: String): Long? {
        return localDataSource.getGroupIdByName(name)
    }

    suspend fun getGroupByName(groupName: String): DomainGroup? {
        return localDataSource.getGroupByName(groupName)
    }

    suspend fun deleteGroupByName(groupName: String): Int {
        val group = localDataSource.getGroupByName(groupName)
        Timber.d("FeedRepository.deleteGroupByName(%s) = %s", groupName, group?.id ?: 0)
        return if (group != null) {
            localDataSource.deleteGroup(group.toRoomGroup())
        } else {
            0
        }
    }

    suspend fun getGroups(): List<DomainGroup>? = localDataSource.getGroups()

    /**
     * FEED
     */
    suspend fun saveLocalFeed(feed: DomainFeed): Long {
        return localDataSource.saveFeed(feed)
    }

    suspend fun saveLocalChannel(channel: DomainChannel): Long {
        return localDataSource.saveChannel(channel)
    }

    suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int {
        return localDataSource.updateFeedFavoriteState(id, favorite)
    }

    fun getGroupsWithFeeds(): Flow<HashMap<String, List<String>>> {
        return localDataSource.getGroupsWithFeeds()
    }

    suspend fun getFeedByLinkName(linkName: String): DomainFeed? {
        return localDataSource.getFeedWithLinkName(linkName)
    }

    suspend fun getFeedIdByLink(link: String): Long? {
        return localDataSource.getFeedIdByLink(link)
    }

    suspend fun getAllFeeds(): List<DomainFeed>? {
        return localDataSource.getAllFeeds()
    }

    suspend fun deleteFeed(feed: DomainFeed): Int {
        return localDataSource.deleteFeed(feed)
    }

    suspend fun deleteGroup(group: Group): Int {
        return localDataSource.deleteGroup(group)
    }

    /**
     * Item
     */
    suspend fun getItemWithFeed(id: Long): DomainItemWithFeed? {
        return localDataSource.getItemWithFeed(id)
    }

    fun getItemWithFeedFlow(id: Long): Flow<DomainItemWithFeed> =
        localDataSource.getItemWithFeedFlow(id)

    suspend fun updateReadStatus(id: Long, read: Boolean): Int {
        return localDataSource.updateReadStatus(id, read)
    }

    suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int {
        return localDataSource.updateReadLaterStatus(id, readLater)
    }

    suspend fun updateInverseReadLaterStatus(id: Long): Int {
        return localDataSource.updateInverseReadLaterStatus(id)
    }

    suspend fun updateGroupFeedReadStatus(gropId: Long): Int {
        return localDataSource.updateGroupFeedReadStatus(gropId)
    }

    suspend fun updateMarkAllFeedAsRead(selectedFeedOptions: SelectedFeedOptions): Int {
        return localDataSource.updateMarkAllFeedAsRead(selectedFeedOptions)
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
    fun getGroupsFlow(): Flow<List<DomainGroup>?>
    suspend fun getGroups(): List<DomainGroup>?
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
    suspend fun getAllFeedsFlow(): Flow<List<DomainFeed>>
    suspend fun getAllFeeds(): List<DomainFeed>?
    suspend fun getFeedIdByLink(link: String): Long?
    suspend fun getFeedWithLinkName(linkName: String): DomainFeed?
    suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int
    suspend fun deleteFeed(feed: DomainFeed): Int

    /**
     * Channel
     */
    //suspend fun channelIsEmpty(): Boolean
    //suspend fun channelSize(): Int
    suspend fun saveChannel(channel: DomainChannel): Long
    suspend fun saveChannelFromServer(channel: ServerChannel): Long
    suspend fun getChannel(feedId: Long): Flow<DomainChannel>
    suspend fun getChannelIdByFeedId(feedId: Long): Long

    /**
     * Item
     */
    suspend fun itemsIsEmpty(): Boolean
    suspend fun itemsSize(): Int
    suspend fun saveItems(items: List<DomainItem>)
    suspend fun saveItemsFromServer(items: List<ServerItem>)
    suspend fun getItems(): Flow<List<DomainItem>>
    suspend fun getItemWithFeed(id: Long): DomainItemWithFeed?
    fun getItemWithFeedFlow(id: Long): Flow<DomainItemWithFeed>
    fun getItemsWithFeed(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainItemWithFeed>>
    suspend fun updateReadStatus(id: Long, read: Boolean): Int
    suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int
    suspend fun updateInverseReadLaterStatus(id: Long): Int
    suspend fun updateGroupFeedReadStatus(gropId: Long): Int
    suspend fun updateMarkAllFeedAsRead(selectedFeedOptions: SelectedFeedOptions): Int
}

interface RemoteDataSource {

    //suspend fun getFeedInfo(): List<DomainItem>
    suspend fun getFeedsFromUrl(apiBaseUrl: String): RssResponse<ServerFeed>

}