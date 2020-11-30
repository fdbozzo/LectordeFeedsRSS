package com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item

import androidx.lifecycle.LiveData
import androidx.room.*

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
    suspend fun insert(feedChannelItem: FeedChannelItem): Long

    /**
     *
     * @param feedChannelItem nuevo valor a reemplazar
     */
    @Update
    suspend fun update(feedChannelItem: FeedChannelItem): Int

    /**
     *
     * @param key Id del FeedChannelItem a buscar
     */
    @Query("SELECT * from feed_channel_item_table WHERE id = :key")
    suspend fun get(key: Long): FeedChannelItem

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_channel_item_table")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla no leídos,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM feed_channel_item_table WHERE read = 0 ORDER BY pub_date DESC")
    fun getAllFeedChannelItems(): LiveData<List<FeedChannelItem>>

    /**
     * Selecciona y retorna el último item.
     */
    @Query("SELECT * FROM feed_channel_item_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastFeedItem(): FeedChannelItem?

    /**
     * Selecciona y retorna el content con el Id indicado.
     */
    @Query("SELECT * from feed_channel_item_table WHERE id = :key")
    fun getFeedItemWithId(key: Long): LiveData<FeedChannelItem>

    /**
     * Selecciona y retorna el content con el link indicado.
     */
    @Query("SELECT * from feed_channel_item_table WHERE link = :link")
    fun getFeedItemWithLink(link: String): LiveData<FeedChannelItem>
}
