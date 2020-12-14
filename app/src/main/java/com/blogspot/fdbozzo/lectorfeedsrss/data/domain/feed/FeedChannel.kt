package com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed

import java.util.*

data class FeedChannel(

    var id: Long = 0L,
    var feedId: Long = 0L,
    var title: String = "",
    var description: String = "",
    var copyright: String = "",
    var link: String = "",
    var pubDate: Date = Date(),
    var channelItems: List<FeedChannelItem>? = null

)