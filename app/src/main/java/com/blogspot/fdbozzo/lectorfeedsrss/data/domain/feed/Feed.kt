package com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed

data class Feed constructor(

    var id: Long = 0L,
    var groupId: Long = 0L,
    var linkName: String = "",
    var link: String = "",
    var favorite: Int = 0,
    var channel: FeedChannel? = null,
    var version: String? = null
)
