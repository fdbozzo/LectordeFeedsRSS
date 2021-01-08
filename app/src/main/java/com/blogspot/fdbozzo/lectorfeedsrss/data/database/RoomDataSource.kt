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
            groupDao.groupCount() <= 0
        }

    override suspend fun groupSize(): Int = withContext(Dispatchers.IO) { groupDao.groupCount() }

    override suspend fun saveGroup(group: DomainGroup): Long {
        var groupId = 0L
        withContext(Dispatchers.IO) {
            groupId = groupDao.insert(group.toRoomGroup())
        }
        return groupId
    }

    override suspend fun getGroupIdByName(name: String): Long {
        var id = 0L
        withContext(Dispatchers.IO) {
            id = groupDao.get(name)
        }
        return id
    }

    override suspend fun getGroupById(key: Long): DomainGroup {
        var group: DomainGroup
        withContext(Dispatchers.IO) {
            val rGroup = groupDao.get(key)
            group = rGroup?.toDomainGroup() ?: DomainGroup()
        }
        return group
    }

    override suspend fun getGroupWithName(name: String): DomainGroup =
        withContext(Dispatchers.IO) {
            groupDao.getGroupWithName(name).toDomainGroup()
        }

    override suspend fun getGroups(): Flow<List<DomainGroup>> =
        withContext(Dispatchers.IO) {
            groupDao.getAllGroups().map { roomGroup ->
                roomGroup.map {
                    it.toDomainGroup()
                }
            }
        }

    override fun deleteGroup(group: Group): Int {
        return groupDao.delete(group)
    }

    override suspend fun deleteAllGroups(): Int {
        var cant = 0
        withContext(Dispatchers.IO) {
            cant = groupDao.deleteAll()
        }
        return cant
    }

    /**
     * FEED
     */
    override suspend fun feedIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun feedSize(): Int = withContext(Dispatchers.IO) { feedDao.feedCount() }

    override suspend fun saveFeed(feed: DomainFeed): Long {
        var feedId = 0L
        withContext(Dispatchers.IO) {
            feedId = feedDao.insert(feed.toRoomFeed())
        }
        return feedId
    }

    override suspend fun saveFeedFromServer(feed: ServerFeed): Long {
        var feedId = 0L
        withContext(Dispatchers.IO) {
            feedId = feedDao.insert(feed.toRoomFeed())
        }
        return feedId
    }

    override suspend fun getFeeds(): Flow<List<DomainFeed>> =
        withContext(Dispatchers.IO) {
            feedDao.getAllFeeds().map { roomFeed ->
                roomFeed.map {
                    it.toDomainFeed()
                }
            }
        }

    override suspend fun getGroupsWithFeeds(): HashMap<String, List<String>> {
        var lista: HashMap<String, List<String>> = HashMap()

        withContext(Dispatchers.IO) {
            lista = (feedDao.getGroupsWithFeedPairs()?.groupByTo(
                HashMap(),
                { it.group.groupName },
                { it.feed?.linkName }
            ) as HashMap<String, List<String>>?)!!

        }

        return lista
    }

    override suspend fun updateFeedFavoriteState(id: Long, favorite: Boolean): Int {
        return feedDao.updateFeedFavoriteState(id, favorite.toInt())
    }

    override suspend fun deleteFeed(feed: DomainFeed): Int {
        return feedDao.delete(feed.toRoomFeed())
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

    override suspend fun getFeedChannel(feedId: Int): Flow<DomainFeedChannel> =
        withContext(Dispatchers.IO) {
            feedChannelDao.get(feedId).map {
                it.toDomainFeedChannel()
            }
        }

    override suspend fun getFeedIdByLink(link: String): Long {
        var feedId = 0L
        withContext(Dispatchers.IO) {
            feedId = feedDao.get(link)
        }
        return feedId
    }

    override suspend fun getFeedChannelIdByFeedId(feedId: Long): Long {
        var feedChannelId = 0L
        withContext(Dispatchers.IO) {
            feedChannelId = feedChannelDao.getFeedChannelIdByFeedId(feedId)
        }
        return feedChannelId
    }

    override fun getFeedWithLinkName(linkName: String): DomainFeed =
        feedDao.getFeedWithLinkName(linkName).toDomainFeed()

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

    /*
    override fun getFeedChannelItemWithFeed(id: Long): Flow<DomainFeedChannelItemWithFeed> =
        //withContext(Dispatchers.IO) {
            feedChannelItemDao.getFeedChannelItemWithFeed(id).map {
                it.toDomainFeedChannelItemWithFeed()
            }
        //}
     */

    override suspend fun getFeedChannelItemWithFeed(id: Long): DomainFeedChannelItemWithFeed? =
        feedChannelItemDao.getFeedChannelItemWithFeed(id)?.toDomainFeedChannelItemWithFeed()

    override fun getFeedChannelItemWithFeedFlow(id: Long): Flow<DomainFeedChannelItemWithFeed> {
        return feedChannelItemDao.getFeedChannelItemWithFeedFlow(id).map {
            feedChannelItemWithFeed -> feedChannelItemWithFeed.toDomainFeedChannelItemWithFeed()
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
        return feedChannelItemDao.updateReadStatus(id, read.toInt())
    }

    override suspend fun updateReadLaterStatus(id: Long, readLater: Boolean): Int {
        return feedChannelItemDao.updateReadLaterStatus(id, readLater.toInt())
    }

    override fun updateInverseReadLaterStatus(id: Long): Int {
        return feedChannelItemDao.updateInverseReadLaterStatus(id)
    }

    override suspend fun updateFeedReadStatus(feedId: Long): Int {
        return feedChannelItemDao.updateFeedReadStatus(feedId)
    }

    override suspend fun updateGroupFeedReadStatus(gropId: Long): Int {
        return  feedChannelItemDao.updateGroupFeedReadStatus(gropId)
    }
}