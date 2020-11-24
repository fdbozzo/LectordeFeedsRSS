package com.blogspot.fdbozzo.lectorfeedsrss.database.content

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed

/**
 * Representa un contenido (noticia).
 */
@Entity(tableName = "content_table",
    foreignKeys = [ForeignKey(
        entity = Feed::class,
        parentColumns = ["id"],
        childColumns = ["feed_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Content(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    var title: String = "",

    @ColumnInfo(index = true)
    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    var pubDate: String = "",

    var description: String = "",

    var read: Int = 0,

    @ColumnInfo(name = "read_later")
    var readLater: Int = 0
)
