package com.blogspot.fdbozzo.lectorfeedsrss.database.group

import androidx.room.*
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed

/**
 * Representa un grupo.
 */
@Entity(
    tableName = "group_table",
    indices = [Index(name = "group_name__group_table", value = ["group_name"], unique = true)]
)
data class Group(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "group_name")
    var groupName: String = "other"

)
