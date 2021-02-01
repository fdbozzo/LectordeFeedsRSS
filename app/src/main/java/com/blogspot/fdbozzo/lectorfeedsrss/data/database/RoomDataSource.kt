package com.blogspot.fdbozzo.lectorfeedsrss.data.database

import com.blogspot.fdbozzo.lectorfeedsrss.data.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.LocalDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Channel as DomainChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.ItemWithFeed as DomainItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Channel as ServerChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Item as ServerItem

class RoomDataSource(db: FeedDatabase) : LocalDataSource {

    private val feedDao = db.getFeedDao()
    private val channelDao = db.getChannelDao()
    private val itemDao = db.getItemDao()
    private val groupDao = db.getGroupDao()

    /**
     * GROUP
     */
    override suspend fun groupIsEmpty(): Boolean =
        withContext(Dispatchers.IO) {
            val cnt = groupDao.groupCount()
            println("[Timber] groupIsEmpty(): cnt = $cnt")
            return@withContext cnt <= 0
        }

    override suspend fun groupSize(): Long = withContext(Dispatchers.IO) {groupDao.groupCount()}

    override suspend fun saveGroup(group: DomainGroup): Long {
        var groupId = 0L
        withContext(Dispatchers.IO) {
            groupId = groupDao.insert(group.toRoomGroup())
        }
        return groupId
    }

    override suspend fun updateGroup(group: DomainGroup): Int =
        withContext(Dispatchers.IO) {
            groupDao.update(group.toRoomGroup())
        }

    override suspend fun getGroupIdByName(name: String): Long? {
        val id = withContext(Dispatchers.IO) {
            println("[Timber] getGroupIdByName(name = $name)")
            return@withContext groupDao.getGroupIdByName(name)
        }
        println("[Timber] getGroupIdByName(name = $name): return id = $id")
        return id
    }

    override suspend fun getGroupById(key: Long): DomainGroup {
        var group: DomainGroup
        withContext(Dispatchers.IO) {
            val rGroup = groupDao.getGroupById(key)
            group = rGroup?.toDomainGroup() ?: DomainGroup()
        }
        return group
    }

    override suspend fun getGroupByName(groupName: String): DomainGroup? =
        withContext(Dispatchers.IO) {
            groupDao.getGroupByName(groupName)?.toDomainGroup()
        }

    override fun getGroupsFlow(): Flow<List<DomainGroup>?> =
        groupDao.getAllGroupsFlow().map { roomGroup ->
            roomGroup?.map {
                it.toDomainGroup()
            }
        }

    override suspend fun getGroups(): List<DomainGroup>? =
        withContext(Dispatchers.IO) {
            groupDao.getAllGroups()?.map { roomGroup ->
                roomGroup.toDomainGroup()
            }
        }

    override suspend fun deleteGroup(group: Group): Int {
        return withContext(Dispatchers.IO) {
            groupDao.delete(group)
        }
    }

    override suspend fun deleteAllGroups(): Int {
        return withContext(Dispatchers.IO) {
            groupDao.deleteAll()
        }
    }

    /**
     * FEED
     */
    override suspend fun feedIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun feedSize(): Int = withContext(Dispatchers.IO) { feedDao.feedCount() }

    override suspend fun saveFeed(feed: DomainFeed): Long {
        var feedCnt = 0L
        withContext(Dispatchers.IO) {
//            try {
                feedCnt = feedDao.insert(feed.toRoomFeed())
//            } catch (e: Exception) {
//                Timber.d("[Timber] ERROR: saveFeedFromServer(%s)", feed)
//            }
        }
        return feedCnt
    }

    override suspend fun saveFeedFromServer(feed: ServerFeed): Long {
        var feedCnt = 0L
        withContext(Dispatchers.IO) {
//            try {
                feedCnt = feedDao.insert(feed.toRoomFeed())
//            } catch (e: Exception) {
//                Timber.d("[Timber] ERROR: saveFeedFromServer(%s)", feed)
//            }
        }
        return feedCnt
    }

    override suspend fun getAllFeedsFlow(): Flow<List<DomainFeed>> =
        withContext(Dispatchers.IO) {
            feedDao.getAllFeedsFlow().map { roomFeed ->
                roomFeed.map {
                    it.toDomainFeed()
                }
            }
        }

    override suspend fun getAllFeeds(): List<DomainFeed>? =
        withContext(Dispatchers.IO) {
            feedDao.getAllFeeds()?.map { roomFeed ->
                roomFeed.toDomainFeed()
            }
        }

    @Suppress("UNCHECKED_CAST")
    override fun getGroupsWithFeeds(): Flow<HashMap<String, List<String>>> {
        return feedDao.getGroupsWithFeedPairs().map { list ->
            // Así sale ordenado
            //*
            list.groupBy(
                { it.group.groupName },
                { it.feed?.linkName }
            )
             //*/

            /* Así sale desordenado
            list.groupByTo(
                HashMap(),
                { it.group.groupName },
                { it.feed?.linkName }
            )
             */
        } as Flow<HashMap<String, List<String>>>
    }

    override suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int {
        return withContext(Dispatchers.IO) {feedDao.updateFeedFavoriteState(id, favorite.toInt())}
    }

    override suspend fun deleteFeed(feed: DomainFeed): Int {
        return withContext(Dispatchers.IO) {feedDao.delete(feed.toRoomFeed())}
    }

    /**
     * CHANNEL
     */
    /*
    override suspend fun channelIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }
     */

    /*
    override suspend fun channelSize(): Int {
        TODO("Not yet implemented")
    }
     */

    override suspend fun saveChannel(channel: DomainChannel): Long {
        var channelId = 0L
        withContext(Dispatchers.IO) {
            channelId = channelDao.insert(channel.toRoomChannel())
        }
        return channelId
    }

    override suspend fun saveChannelFromServer(channel: ServerChannel): Long {
        var channelId = 0L
        withContext(Dispatchers.IO) {
            channelId = channelDao.insert(channel.toRoomChannel())
        }
        return channelId
    }

    override suspend fun getChannel(feedId: Long): Flow<DomainChannel> =
        withContext(Dispatchers.IO) {
            channelDao.get(feedId).map {
                it.toDomainChannel()
            }
        }

    override suspend fun getFeedIdByLink(link: String): Long? {
        return withContext(Dispatchers.IO) {
            feedDao.get(link)
        }
    }

    override suspend fun getChannelIdByFeedId(feedId: Long): Long {
        return withContext(Dispatchers.IO) {
            channelDao.getChannelIdByFeedId(feedId)
        }
    }

    override suspend fun getFeedWithLinkName(linkName: String): DomainFeed? =
        withContext(Dispatchers.IO) { feedDao.getFeedWithLinkName(linkName)?.toDomainFeed() }

    /**
     * FEED-CHANNEL-ITEM
     */
    override suspend fun itemsIsEmpty(): Boolean =
        withContext(Dispatchers.IO) { itemDao.itemCount() <= 0 }

    override suspend fun itemsSize(): Int =
        withContext(Dispatchers.IO) { itemDao.itemCount() }

    override suspend fun saveItems(items: List<DomainItem>) {
        withContext(Dispatchers.IO) {
            itemDao.insert(items.map { domainItem ->
                domainItem.toRoomItem()
            })
        }
    }

    override suspend fun saveItemsFromServer(items: List<ServerItem>) {
        withContext(Dispatchers.IO) {
            itemDao.insert(items.map { serverItem ->
                //Timber.d("[Timber] serverItem = %s", serverItem.toString())
                serverItem.toRoomItem()
            })
        }
    }

    override suspend fun getItems(): Flow<List<DomainItem>> =
        withContext(Dispatchers.IO) {
            itemDao.getAllItems().map { roomItem ->
                roomItem.map {
                    it.toDomainItem()
                }
            }
        }

    override suspend fun getItemWithFeed(id: Long): DomainItemWithFeed? =
        withContext(Dispatchers.IO) {
            itemDao.getItemWithFeed(id)?.toDomainItemWithFeed()
        }

    override fun getItemWithFeedFlow(id: Long): Flow<DomainItemWithFeed> {
        return itemDao.getItemWithFeedFlow(id).map { itemWithFeed ->
            itemWithFeed.toDomainItemWithFeed()
        }
    }

    override fun getItemsWithFeed(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainItemWithFeed>> =
        itemDao.getFilteredItemsWithFeed(
            linkName = selectedFeedOptions.linkName,
            favorite = selectedFeedOptions.favorite.toInt(),
            readLater = selectedFeedOptions.readLater.toInt(),
            read = selectedFeedOptions.read.toInt()
        ).map { roomItemWithFeed ->
            roomItemWithFeed.map {
                it.toDomainItemWithFeed()
            }
        }

    override suspend fun updateReadStatus(id: Long, read: Boolean): Int {
        return withContext(Dispatchers.IO) {itemDao.updateReadStatus(id, read.toInt())}
    }

    override suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int {
        return withContext(Dispatchers.IO) {itemDao.updateReadLaterStatus(id, readLater.toInt())}
    }

    override suspend fun updateInverseReadLaterStatus(id: Long): Int {
        return withContext(Dispatchers.IO) {itemDao.updateInverseReadLaterStatus(id)}
    }

    override suspend fun updateGroupFeedReadStatus(gropId: Long): Int {
        return withContext(Dispatchers.IO) {itemDao.updateGroupFeedReadStatus(gropId)}
    }

    override suspend fun updateMarkAllFeedAsRead(selectedFeedOptions: SelectedFeedOptions): Int {
        return withContext(Dispatchers.IO) {itemDao.updateMarkAllFeedAsRead(
            linkName = selectedFeedOptions.linkName,
            favorite = selectedFeedOptions.favorite.toInt(),
            readLater = selectedFeedOptions.readLater.toInt(),
            read = selectedFeedOptions.read.toInt()
        )}
    }
}