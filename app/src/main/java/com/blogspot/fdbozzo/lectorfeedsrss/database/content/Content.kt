package com.blogspot.fdbozzo.lectorfeedsrss.database.content

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un contenido (noticia).
 */
@Entity(tableName = "content_table")
data class Content(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "feed_id", index = true)
    val feedId: Long = 0L,

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
