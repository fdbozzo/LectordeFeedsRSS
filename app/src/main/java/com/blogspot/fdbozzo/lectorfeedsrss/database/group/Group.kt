package com.blogspot.fdbozzo.lectorfeedsrss.database.group

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed

/**
 * Representa un grupo.
 */
@Entity(
    tableName = "group_table",
    indices = [Index(name = "groupname__group_table", value = ["group_name"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Feed::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Group(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "group_name")
    var groupName: String = "other"

)
