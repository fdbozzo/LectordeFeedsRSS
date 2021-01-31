package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem
import java.util.*

/**
 * Entidad Item (noticia o item del feed).
 */
@Entity(
    tableName = "item_table",
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
        )
    ]
)
data class Item(

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0L,

    var title: String = "",

    var link: String = "",

    @ColumnInfo(name = "pub_date", index = true)
    var pubDate: Date? = null,

    var description: String = "",

    @ColumnInfo(defaultValue = "0")
    var read: Int = 0,

    @ColumnInfo(name = "read_later", defaultValue = "0")
    var readLater: Int = 0,

    @ColumnInfo(name = "image_link")
    var imageLink: String = ""

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}