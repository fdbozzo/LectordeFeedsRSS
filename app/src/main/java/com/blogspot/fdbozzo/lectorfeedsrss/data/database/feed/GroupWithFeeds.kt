package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Esta clase se usa para los items (grupo / feed) del menú del Drawer
 */
data class GroupWithFeeds(
    @Embedded
    val group: Group,

    @Relation(
        parentColumn = "id",
        entityColumn = "group_id"
    )
    val feeds: List<Feed> = emptyList()
)