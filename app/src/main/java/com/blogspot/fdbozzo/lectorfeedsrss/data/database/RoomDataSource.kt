package com.blogspot.fdbozzo.lectorfeedsrss.data.database

import com.blogspot.fdbozzo.lectorfeedsrss.data.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.LocalDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.util.toInt
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannelItem as ServerFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannel as ServerFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.collections.HashMap

class RoomDataSource(db: FeedDatabase) : LocalDataSource {

    private val feedDao = db.getFeedDao()
    private val feedChannelDao = db.getFeedChannelDao()
    private val feedChannelItemDao = db.getFeedChannelItemDao()
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

    override suspend fun getGroups(): Flow<List<DomainGroup>> =
        withContext(Dispatchers.IO) {
            groupDao.getAllGroups().map { roomGroup ->
                roomGroup.map {
                    it.toDomainGroup()
                }
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
            try {
                feedCnt = feedDao.insert(feed.toRoomFeed())
            } catch (e: Exception) {
                Timber.d("[Timber] ERROR: saveFeedFromServer(%s)", feed)
            }
        }
        return feedCnt
    }

    override suspend fun saveFeedFromServer(feed: ServerFeed): Long {
        var feedCnt = 0L
        withContext(Dispatchers.IO) {
            try {
                feedCnt = feedDao.insert(feed.toRoomFeed())
            } catch (e: Exception) {
                Timber.d("[Timber] ERROR: saveFeedFromServer(%s)", feed)
            }
        }
        return feedCnt
    }

    override suspend fun getFeeds(): Flow<List<DomainFeed>> =
        withContext(Dispatchers.IO) {
            feedDao.getAllFeeds().map { roomFeed ->
                roomFeed.map {
                    it.toDomainFeed()
                }
            }
        }

    override fun getGroupsWithFeeds(): Flow<HashMap<String, List<String>>> {
        return feedDao.getGroupsWithFeedPairs().map { list ->
            list.groupByTo(
                HashMap(),
                { it.group.groupName },
                { it.feed?.linkName }
            )
        } as Flow<HashMap<String, List<String>>>
    }

    override suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int {
        return withContext(Dispatchers.IO) {feedDao.updateFeedFavoriteState(id, favorite.toInt())}
    }

    override suspend fun deleteFeed(feed: DomainFeed): Int {
        return withContext(Dispatchers.IO) {feedDao.delete(feed.toRoomFeed())}
    }

    /**
     * FEED-CHANNEL
     */
    /*
    override suspend fun feedChannelIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }
     */

    /*
    override suspend fun feedChannelSize(): Int {
        TODO("Not yet implemented")
    }
     */

    override suspend fun saveFeedChannel(feedChannel: DomainFeedChannel): Long {
        var feedChannelId = 0L
        withContext(Dispatchers.IO) {
            feedChannelId = feedChannelDao.insert(feedChannel.toRoomFeedChannel())
        }
        return feedChannelId
    }

    override suspend fun saveFeedChannelFromServer(feedChannel: ServerFeedChannel): Long {
        var feedChannelId = 0L
        withContext(Dispatchers.IO) {
            feedChannelId = feedChannelDao.insert(feedChannel.toRoomFeedChannel())
        }
        return feedChannelId
    }

    override suspend fun getFeedChannel(feedId: Long): Flow<DomainFeedChannel> =
        withContext(Dispatchers.IO) {
            feedChannelDao.get(feedId).map {
                it.toDomainFeedChannel()
            }
        }

    override suspend fun getFeedIdByLink(link: String): Long {
        return withContext(Dispatchers.IO) {
            feedDao.get(link)
        }
    }

    override suspend fun getFeedChannelIdByFeedId(feedId: Long): Long {
        return withContext(Dispatchers.IO) {
            feedChannelDao.getFeedChannelIdByFeedId(feedId)
        }
    }

    override suspend fun getFeedWithLinkName(linkName: String): DomainFeed =
        withContext(Dispatchers.IO) {feedDao.getFeedWithLinkName(linkName).toDomainFeed()}

    /**
     * FEED-CHANNEL-ITEM
     */
    override suspend fun feedChannelItemsIsEmpty(): Boolean =
        withContext(Dispatchers.IO) { feedChannelItemDao.feedChannelItemCount() <= 0 }

    override suspend fun feedChannelItemsSize(): Int =
        withContext(Dispatchers.IO) { feedChannelItemDao.feedChannelItemCount() }

    override suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>) {
        withContext(Dispatchers.IO) {
            feedChannelItemDao.insert(feedChannelItems.map { domainFeedChannelItem ->
                domainFeedChannelItem.toRoomFeedChannelItem()
            })
        }
    }

    override suspend fun saveFeedChannelItemsFromServer(feedChannelItems: List<ServerFeedChannelItem>) {
        withContext(Dispatchers.IO) {
            feedChannelItemDao.insert(feedChannelItems.map { serverFeedChannelItem ->
                Timber.d("[Timber] serverFeedChannelItem = %s", serverFeedChannelItem.toString())
                serverFeedChannelItem.toRoomFeedChannelItem()
            })
        }
    }

    override suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>> =
        withContext(Dispatchers.IO) {
            feedChannelItemDao.getAllFeedChannelItems().map { roomFeedChannelItem ->
                roomFeedChannelItem.map {
                    it.toDomainFeedChannelItem()
                }
            }
        }

    override suspend fun getFeedChannelItemWithFeed(id: Long): DomainFeedChannelItemWithFeed? =
        withContext(Dispatchers.IO) {
            feedChannelItemDao.getFeedChannelItemWithFeed(id)?.toDomainFeedChannelItemWithFeed()
        }

    override fun getFeedChannelItemWithFeedFlow(id: Long): Flow<DomainFeedChannelItemWithFeed> {
        return feedChannelItemDao.getFeedChannelItemWithFeedFlow(id).map { feedChannelItemWithFeed ->
            feedChannelItemWithFeed.toDomainFeedChannelItemWithFeed()
        }
    }

    override fun getFeedChannelItemsWithFeed(selectedFeedOptions: SelectedFeedOptions): Flow<List<DomainFeedChannelItemWithFeed>> =
        feedChannelItemDao.getFilteredFeedChannelItemsWithFeed(
            linkName = selectedFeedOptions.linkName,
            favorite = selectedFeedOptions.favorite.toInt(),
            readLater = selectedFeedOptions.readLater.toInt(),
            read = selectedFeedOptions.read.toInt()
        ).map { roomFeedChannelItemWithFeed ->
            roomFeedChannelItemWithFeed.map {
                it.toDomainFeedChannelItemWithFeed()
            }
        }

    override suspend fun updateReadStatus(id: Long, read: Boolean): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateReadStatus(id, read.toInt())}
    }

    override suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateReadLaterStatus(id, readLater.toInt())}
    }

    override suspend fun updateInverseReadLaterStatus(id: Long): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateInverseReadLaterStatus(id)}
    }

    override suspend fun updateFeedReadStatus(feedId: Long): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateFeedReadStatus(feedId)}
    }

    override suspend fun updateGroupFeedReadStatus(gropId: Long): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateGroupFeedReadStatus(gropId)}
    }

    override suspend fun updateMarkAllFeedAsRead(): Int {
        return withContext(Dispatchers.IO) {feedChannelItemDao.updateMarkAllFeedAsRead()}
    }
}