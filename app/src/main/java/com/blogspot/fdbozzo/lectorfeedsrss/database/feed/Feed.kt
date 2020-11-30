package com.blogspot.fdbozzo.lectorfeedsrss.database.feed

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.FeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.Group
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * Entidad Feed - link y nombre del Feed a mostrar
 */
@Entity(
    tableName = "feed_table",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Root(name = "rss", strict = false)
data class Feed constructor(

    @ColumnInfo(name = "group_id", index = true)
    var groupId: Long = 0L,

    @ColumnInfo(name = "link_name", index = true)
    var linkName: String = "",

    var link: String = "",

    var favorite: Int = 0,

    @Ignore
    @field:Element(name = "channel", required = false)
    //@field:ElementList(name = "channel", inline = true, required = false)
    var channel: FeedChannel? = null,

    @Ignore
    var version: String? = null
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}
