package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository

/**
 * Simple view model factory for RssItemListViewModel
 */
//*
class FeedChannelViewModelFactory (
        private val feedRepository: FeedRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FeedChannelViewModel(feedRepository) as T
        }

}
 //*/

/*
class FeedChannelViewModelFactory (
    private val daoChannel: FeedChannelItemDao
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeedChannelViewModel(daoChannel) as T
    }

}
 */