package com.blogspot.fdbozzo.lectorfeedsrss.database

import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FeedRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    //suspend fun getFeeds(): List<FeedChannelItem> {
    fun getFeeds(): Flow<List<FeedChannelItem>> = flow {

        if (localDataSource.isEmpty()) {
            val feedChannelItem = remoteDataSource.getFeeds()
            localDataSource.saveFeedChannelItems(feedChannelItem)
        }

        //return localDataSource.getFeedChannelItems()
        emit(localDataSource.getFeedChannelItems())
    }


}

interface LocalDataSource {

    suspend fun isEmpty(): Boolean

    suspend fun saveFeedChannelItems(feedChannelItem: List<FeedChannelItem>): Unit {
        // TODO: implementar
    }

    fun getFeedChannelItems(): List<FeedChannelItem> {
        // TODO: implementar
        return emptyList()
    }

}

interface RemoteDataSource {

    suspend fun getFeeds(): List<FeedChannelItem> {
        // TODO: implementar
        return emptyList()
    }

}