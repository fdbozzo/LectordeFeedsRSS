package com.blogspot.fdbozzo.lectorfeedsrss.network.feed

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.*

@Root(name = "channel", strict = false)
data class FeedChannel(

    var id: Long = 0L,

    var feedId: Long = 0L,

    var title: String = "",

    var description: String = "",

    var copyright: String = "",

    var link: String = "",

    var pubDate: Date = Date(),

    @field:ElementList(name = "item", inline = true, required = false)
    var channelItems: List<FeedChannelItem>? = null

)
