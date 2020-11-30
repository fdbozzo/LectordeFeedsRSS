package com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.FeedChannel
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
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
@Root(name = "item", strict = false)
data class FeedChannelItem(

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    @field:Element(name = "title")
    var title: String = "",

    @ColumnInfo(index = true)
    @field:Element(name = "link")
    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    //@field:Element(name = "pubDate")
    var pubDate: Date = Date(),

    @field:Element(name = "description")
    var description: String = "",

    var read: Int = 0,

    @ColumnInfo(name = "read_later")
    var readLater: Int = 0,

    @ColumnInfo(name = "image_link")
    var imageLink: String = ""

    /*
    @field:Element(name = "guid")
    var guid: String? = null,
     */


) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
