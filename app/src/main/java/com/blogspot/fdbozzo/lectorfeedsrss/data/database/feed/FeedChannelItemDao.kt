package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase FeedChannelItem con Room.
 */
@Dao
interface FeedChannelItemDao {

    /**
     *
     * @param feedChannelItem nuevo valor a insertar
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feedChannelItem: FeedChannelItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feedChannelItems: List<FeedChannelItem>)

    /**
     *
     * @param feedChannelItem nuevo valor a reemplazar
     */
    @Update
    fun update(feedChannelItem: FeedChannelItem): Int

    @Query("UPDATE feed_channel_item_table SET read = :read WHERE id = :id")
    suspend fun updateReadStatus(id: Long ,read: Int): Int

    @Query("UPDATE feed_channel_item_table SET read_later = :readLater WHERE id = :id")
    suspend fun updateReadLaterStatus(id: Long ,readLater: Int): Int

    @Query("UPDATE feed_channel_item_table SET read_later = (1 - read_later) WHERE id = :id")
    fun updateInverseReadLaterStatus(id: Long): Int

    /**
     *
     * @param key Id del FeedChannelItem a buscar
     */
    @Query("SELECT * from feed_channel_item_table WHERE id = :key")
    fun get(key: Long): FeedChannelItem

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_channel_item_table")
    fun clear()

    @Query("SELECT COUNT(id) FROM feed_channel_item_table")
    fun feedChannelItemCount(): Int

    /**
     * Selecciona y retorna todos los datos de la tabla no leídos,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM feed_channel_item_table WHERE read = 0 ORDER BY pub_date DESC")
    fun getAllFeedChannelItems(): Flow<List<FeedChannelItem>>

    /**
     * Selecciona y retorna todos los datos de la tabla filtrados y
     * ordenados por fecha de publicación descendente.
     */
    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM feed_channel_item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.read <= :read
        AND fcit.read_later >= :readLater
        AND ft.favorite >= :favorite
        AND ft.link_name LIKE :linkName
        ORDER BY fcit.pub_date DESC"""
    )
    fun getFilteredFeedChannelItemsWithFeed(linkName: String, favorite: Int, readLater: Int, read: Int): Flow<List<FeedChannelItemWithFeed>>

    /**
     * Devuelve el item del id indicado e información extra sobre el feed del mismo
     */
    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM feed_channel_item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.id = :id"""
    )
    suspend fun getFeedChannelItemWithFeed(id: Long): FeedChannelItemWithFeed?

    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM feed_channel_item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.id = :id"""
    )
    fun getFeedChannelItemWithFeedFlow(id: Long): Flow<FeedChannelItemWithFeed>

    /**
     * Selecciona y retorna el último item.
     */
    @Query("SELECT * FROM feed_channel_item_table ORDER BY id DESC LIMIT 1")
    fun getLastFeedItem(): FeedChannelItem?

    /**
     * Selecciona y retorna el contentEncoded con el Id indicado.
     */
    /*
    @Query("SELECT * from feed_channel_item_table WHERE id = :key")
    fun getFeedItemWithId(key: Long): Flow<FeedChannelItem>

     */

    /**
     * Selecciona y retorna el contentEncoded con el link indicado.
     */
    /*
    @Query("SELECT * from feed_channel_item_table WHERE link = :link")
    fun getFeedItemWithLink(link: String): Flow<FeedChannelItem>

     */
}
