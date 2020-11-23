package com.blogspot.fdbozzo.lectorfeedsrss.database.feed

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.content.Content

/**
 * Representa un feed.
 */
@Entity(
    tableName = "feed_table",
    indices = [Index(name = "feedid_linkname__feed_table", value = ["id","link_name"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Content::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Feed(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "group_id", index = true)
    val groupId: Long = 0L,

    @ColumnInfo(name = "link_name", index = true)
    var linkName: String = "",

    var link: String = "",

    var favorite: Int = 0
)
