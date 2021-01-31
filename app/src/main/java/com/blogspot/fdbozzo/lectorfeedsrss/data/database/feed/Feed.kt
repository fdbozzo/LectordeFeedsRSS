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
    )],
    indices = [
        Index(
            value = ["link"],
            unique = true
        ),
        Index(
            value = ["link_name"],
            unique = true
        )
    ]
)
data class Feed constructor(

    @ColumnInfo(name = "group_id", index = true)
    var groupId: Long = 1L,

    @ColumnInfo(name = "link_name", index = false)
    var linkName: String = "",

    @ColumnInfo(name = "link")
    var link: String = "",

    @ColumnInfo(defaultValue = "0")
    var favorite: Int = 0,

    @Ignore
    var channel: Channel? = null,

    @Ignore
    var version: String? = null
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    /**
     *
     */
    class GroupWithFeedPair {
        @Embedded(prefix = "grp_")
        var group: Group = Group()

        @Embedded
        var feed: Feed? = null //Feed()
    }

}
