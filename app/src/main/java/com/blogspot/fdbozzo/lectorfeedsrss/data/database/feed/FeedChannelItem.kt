package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import java.util.*

/**
 * Entidad FeedChannelItem (noticia o item del feed).
 */
@Entity(
    tableName = "feed_channel_item_table",
    foreignKeys = [ForeignKey(
        entity = Feed::class,
        parentColumns = ["id"],
        childColumns = ["feed_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FeedChannelItem(

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    var title: String = "",

    @ColumnInfo(index = true)
    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    //@field:Element(name = "pubDate")
    var pubDate: Date? = null,

    var description: String = "",

    var read: Int = 0,

    @ColumnInfo(name = "read_later")
    var readLater: Int = 0,

    @ColumnInfo(name = "image_link")
    var imageLink: String = ""

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
