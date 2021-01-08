package com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed

import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group

data class Group(
    var id: Long = 0L,
    var groupName: String = Group.DEFAULT_NAME // "Uncategorized"

)