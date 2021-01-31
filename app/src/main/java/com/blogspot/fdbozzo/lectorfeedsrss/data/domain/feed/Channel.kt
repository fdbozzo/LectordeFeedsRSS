package com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed

import java.util.*

data class Channel(

    var id: Long = 0L,
    var feedId: Long = 0L,
    var title: String = "",
    var description: String = "",
    var copyright: String = "",
    var link: String = "",
    var pubDate: Date = Date(),
    var items: List<Item>? = null

)