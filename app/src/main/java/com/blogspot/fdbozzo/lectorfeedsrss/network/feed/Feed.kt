package com.blogspot.fdbozzo.lectorfeedsrss.network.feed

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class Feed constructor(

    var id: Long = 0L,

    var groupId: Long = 0L,

    var linkName: String = "",

    var link: String = "",

    var favorite: Int = 0,

    @field:Element(name = "channel", required = true)
    var channel: FeedChannel = FeedChannel(),

    var version: String? = null
)
