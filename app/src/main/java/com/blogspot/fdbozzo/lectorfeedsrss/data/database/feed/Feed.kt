package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*

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
data class Feed constructor(

    @ColumnInfo(name = "group_id", index = true)
    var groupId: Long = 0L,

    @ColumnInfo(name = "link_name", index = true)
    var linkName: String = "",

    var link: String = "",

    var favorite: Int = 0,

    @Ignore
    var channel: FeedChannel? = null,

    @Ignore
    var version: String? = null
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}
