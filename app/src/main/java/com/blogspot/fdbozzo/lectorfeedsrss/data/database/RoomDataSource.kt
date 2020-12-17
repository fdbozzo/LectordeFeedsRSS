package com.blogspot.fdbozzo.lectorfeedsrss.data.database

import com.blogspot.fdbozzo.lectorfeedsrss.data.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.LocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomDataSource(db: FeedDatabase) : LocalDataSource {

    private val feedDao = db.getFeedDao()
    private val feedChannelDao = db.getFeedChannelDao()
    private val feedChannelItemDao = db.getFeedChannelItemDao()
    private val groupDao = db.getGroupDao()

    /** GROUP **/
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

    override suspend fun getGroupId(name: String): Long =
        withContext(Dispatchers.IO) {
            groupDao.get(name)
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

    /** FEED **/
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

    override suspend fun getFeeds(): Flow<List<DomainFeed>> =
        withContext(Dispatchers.IO) {
            feedDao.getAllFeeds().map { roomFeed ->
                roomFeed.map {
                    it.toDomainFeed()
                }
            }
        }

    /** FEED-CHANNEL **/
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

    /** FEED-CHANNEL-ITEM **/
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

    override suspend fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>> =
        withContext(Dispatchers.IO) {
            feedChannelItemDao.getAllFeedChannelItems().map { roomFeedChannelItem ->
                roomFeedChannelItem.map {
                    it.toDomainFeedChannelItem()
                }
            }
        }

}