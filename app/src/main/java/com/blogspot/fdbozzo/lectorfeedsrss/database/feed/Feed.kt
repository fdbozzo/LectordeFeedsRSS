package com.blogspot.fdbozzo.lectorfeedsrss.database.feed

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.content.Content
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.Group

/**
 * Representa un feed.
 */
@Entity(
    tableName = "feed_table",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Feed(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "group_id", index = true)
    var groupId: Long = 0L,

    @ColumnInfo(name = "link_name", index = true)
    var linkName: String = "",

    var link: String = "",

    var favorite: Int = 0
)
