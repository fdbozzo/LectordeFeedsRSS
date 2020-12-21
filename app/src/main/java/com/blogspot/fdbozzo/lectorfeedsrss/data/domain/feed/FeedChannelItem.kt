package com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed

import java.util.*

data class FeedChannelItem(
    var id: Long = 0L,
    var feedId: Long = 0L,
    var title: String = "",
    var link: String = "",
    var pubDate: Date? = null,
    var description: String = "",
    var content: String = "",
    var read: Int = 0,
    var readLater: Int = 0,
    var imageLink: String = ""

)