package com.blogspot.fdbozzo.lectorfeedsrss.data.database

import com.blogspot.fdbozzo.lectorfeedsrss.data.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.LocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(db: FeedDatabase) : LocalDataSource {

    private val feedDao = db.getFeedDao()
    private val feedChannelDao = db.getFeedChannelDao()
    private val feedChannelItemDao = db.getFeedChannelItemDao()
    private val groupDao = db.getGroupDao()

    override suspend fun groupIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun groupSize(): Int = groupDao.groupCount()

    /*
    override suspend fun saveGroup(group: DomainGroup) {
        groupDao.insert(group.toRoomGroup())
    }
     */

    override fun getGroups(): Flow<List<DomainGroup>> =
        groupDao.getAllGroups().map {
            roomGroup -> roomGroup.map {
                it.toDomainGroup()
            }
        }

    override suspend fun feedIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun feedSize(): Int = feedDao.feedCount()

    /*
    override suspend fun saveFeed(feed: DomainFeed) {
        feedDao.insert(feed.toRoomFeed())
    }
     */

    override fun getFeeds(): Flow<List<DomainFeed>> =
        feedDao.getAllFeeds().map {
            roomFeed -> roomFeed.map {
                it.toDomainFeed()
            }
        }

    override suspend fun feedChannelIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun feedChannelSize(): Int {
        TODO("Not yet implemented")
    }

    /*
    override suspend fun saveFeedChannels(feedChannels: List<DomainFeedChannel>) {
        feedChannelDao.insert(feedChannels.map {
            domainFeedChannel -> domainFeedChannel.toRoomFeedChannel()
        })
    }
     */

    override fun getFeedChannels(): Flow<List<DomainFeedChannel>> =
        feedChannelDao.getAll().map {
            roomFeedChannel -> roomFeedChannel.map {
                it.toDomainFeedChannel()
            }
        }

    override suspend fun feedChannelItemsIsEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun feedChannelItemsSize(): Int = feedChannelItemDao.feedChannelItemCount()

    /*
    override suspend fun saveFeedChannelItems(feedChannelItems: List<DomainFeedChannelItem>) {
        feedChannelItemDao.insert(feedChannelItems.map {
            domainFeedChannelItem -> domainFeedChannelItem.toRoomFeedChannelItem()
        })
    }
     */

    override fun getFeedChannelItems(): Flow<List<DomainFeedChannelItem>> =
        feedChannelItemDao.getAllFeedChannelItems().map {
            roomFeedChannelItem -> roomFeedChannelItem.map {
                it.toDomainFeedChannelItem()
            }
        }

    /*
    override suspend fun saveMovies(movies: List<Movie>) {
        movieDao.insertMovies(movies.map { it.toRoomMovie() })
    }
    */

    /*
    override fun getMovies(): Flow<List<Movie>> =
        movieDao
            .getAll()
            .map { movies -> movies.map { it.toDomainMovie() } }
     */
}