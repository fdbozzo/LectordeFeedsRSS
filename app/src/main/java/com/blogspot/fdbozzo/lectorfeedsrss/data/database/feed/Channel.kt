package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import java.util.*

/**
 * Entidad Channel - Canal RSS Feed
 */
@Entity(
    tableName = "channel_table",
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feed_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["link"],
            unique = true
        ),
        Index(
            value = ["title"],
            unique = true
        )
    ]
)
data class Channel(

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    var title: String = "",

    var description: String = "",

    var copyright: String = "",

    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    var pubDate: Date = Date(),

    @Ignore
    var items: List<Item>? = null

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    //override fun toString(): String = title
}
