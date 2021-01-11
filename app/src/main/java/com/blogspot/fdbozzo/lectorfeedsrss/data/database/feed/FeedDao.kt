package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase Feed con Room.
 */
@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: Feed): Long

    /**
     * Cuando se actualiza una fila con un valor existente en la columna,
     * se reemplaza el valor antiguo por el nuevo.
     *
     * @param feed nuevo valor a escribir
     */
    @Update
    suspend fun update(feed: Feed): Int

    /**
     * Agrega o quita el feed de favoritos
     */
    @Query("UPDATE feed_table SET favorite = :favorite WHERE id = :id")
    suspend fun updateFeedFavoriteState(id: Long, favorite: Int): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key feed a buscar
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    suspend fun get(key: Long): Feed

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param link feed a buscar
     */
    @Query("SELECT id from feed_table WHERE link = :link")
    suspend fun get(link: String): Long?

    @Delete
    suspend fun delete(feed: Feed): Int

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_table")
    suspend fun clear()

    @Query("SELECT COUNT(id) FROM feed_table")
    suspend fun feedCount(): Int

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por linkName.
     */
    @Query("SELECT * FROM feed_table ORDER BY link_name ASC")
    fun getAllFeeds(): Flow<List<Feed>>

    /**
     * Selecciona y retorna el último feed.
     */
    @Query("SELECT * FROM feed_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastFeed(): Feed?

    /**
     * Selecciona y retorna el feed con el Id indicado.
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    suspend fun getFeedWithId(key: Long): Feed

    /**
     * Selecciona y retorna el feed con el link indicado.
     */
    @Query("SELECT * from feed_table WHERE link = :link")
    suspend fun getFeedWithLink(link: String): Feed

    /**
     * Selecciona y retorna el feed con el linkName indicado.
     */
    @Query("SELECT * from feed_table WHERE link_name = :linkName")
    suspend fun getFeedWithLinkName(linkName: String): Feed?

    //@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        """SELECT group_table.id as grp_id, 
        group_table.group_name as grp_group_name, 
        feed_table.* 
        FROM group_table 
        LEFT JOIN feed_table ON feed_table.group_id = group_table.id
        ORDER BY group_table.group_name ASC, feed_table.link_name ASC"""
    )
    fun getGroupsWithFeedPairs(): Flow<List<Feed.GroupWithFeedPair>>

}
