package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.util.*

/**
 * Esta clase es para poder mostrar algunos datos más que no está en los items,
 * como link_name
 */
data class FeedChannelItemWithFeed(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

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

)