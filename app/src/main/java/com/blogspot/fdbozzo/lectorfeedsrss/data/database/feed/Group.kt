package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*

/**
 * Entidad Group - Agrupa los Feeds.
 */
@Entity(
    tableName = "group_table",
    indices = [Index(name = "group_name__group_table", value = ["group_name"], unique = true)]
)
data class Group(
    @ColumnInfo(name = "group_name")
    var groupName: String = "Uncategorized"

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}
