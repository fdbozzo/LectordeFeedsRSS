package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.util.*

data class FeedChannelItemWithFeed(

    @ColumnInfo(name = "link_name")
    var linkName: String = "",

    @ColumnInfo(name = "feed_id")
    var feedId: Long = 0L,

    var title: String = "",

    var link: String = "",

    @ColumnInfo(name = "pub_date")
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
