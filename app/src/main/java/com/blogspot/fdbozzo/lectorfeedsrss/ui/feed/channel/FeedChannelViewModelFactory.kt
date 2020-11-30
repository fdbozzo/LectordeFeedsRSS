package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItemDao

/**
 * Simple view model factory for RssItemListViewModel
 */
class FeedChannelViewModelFactory (
        private val daoChannel: FeedChannelItemDao
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FeedChannelViewModel(daoChannel) as T
        }

}