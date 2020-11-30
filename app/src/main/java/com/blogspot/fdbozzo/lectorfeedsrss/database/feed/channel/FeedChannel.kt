package com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.*

/**
 * Entidad FeedChannel - Canal RSS Feed
 */
@Entity(
    tableName = "feed_channel",
    foreignKeys = [ForeignKey(
        entity = Feed::class,
        parentColumns = ["id"],
        childColumns = ["feed_id"],
        onDelete = ForeignKey.CASCADE
    )] /*,
    indices = [
        Index(
            value = ["title", "link"],
            unique = true
        )]*/
)
@Root(name = "channel", strict = false)
data class FeedChannel(

    /*
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
     */

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    var title: String = "",

    var description: String = "",

    var copyright: String = "",

    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    var pubDate: Date = Date(),

    @Ignore
    @field:ElementList(name = "item", inline = true, required = false)
    var channelItems: List<FeedChannelItem>? = null

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    //override fun toString(): String = title
}