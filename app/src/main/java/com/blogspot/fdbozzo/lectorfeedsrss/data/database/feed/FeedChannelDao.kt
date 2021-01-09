package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Rss feed channel DAO
 */
@Dao
interface FeedChannelDao {

    /**
     *
     * @param feedItem nuevo valor a insertar
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feedChannel: FeedChannel) : Long

    /**
     *
     * @param feedItem nuevo valor a reemplazar
     */
    @Update
    suspend fun update(feedChannel: FeedChannel): Int

    /**
     *
     * @param title Título del FeedChannel a buscar
     */
    @Query("SELECT * FROM feed_channel_table WHERE title = :title")
    fun get(title: String) : Flow<FeedChannel>

    /**
     *
     * @param feedId Id del Feed del que buscar su channel
     */
    @Query("SELECT * FROM feed_channel_table WHERE feed_id = :feedId")
    fun get(feedId: Long) : Flow<FeedChannel>

    /**
     *
     * @param feedId Id del Feed del que buscar su channelId
     */
    @Query("SELECT id FROM feed_channel_table WHERE feed_id = :feedId")
    suspend fun getFeedChannelIdByFeedId(feedId: Long) : Long

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_channel_table")
    suspend fun clear()

    @Query("SELECT COUNT(id) FROM feed_channel_table")
    suspend fun groupCount(): Int

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM feed_channel_table ORDER BY pub_date DESC")
    fun getAll() : Flow<List<FeedChannel>>

}