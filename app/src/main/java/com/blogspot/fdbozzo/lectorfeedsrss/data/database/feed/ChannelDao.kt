package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Rss feed channel DAO
 */
@Dao
interface ChannelDao {

    /**
     *
     * @param channel nuevo valor a insertar
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(channel: Channel) : Long

    /**
     *
     * @param channel nuevo valor a reemplazar
     */
    @Update
    suspend fun update(channel: Channel): Int

    /**
     *
     * @param title Título del Channel a buscar
     */
    @Query("SELECT * FROM channel_table WHERE title = :title")
    fun get(title: String) : Flow<Channel>

    /**
     *
     * @param feedId Id del Feed del que buscar su channel
     */
    @Query("SELECT * FROM channel_table WHERE feed_id = :feedId")
    fun get(feedId: Long) : Flow<Channel>

    /**
     *
     * @param feedId Id del Feed del que buscar su channelId
     */
    @Query("SELECT id FROM channel_table WHERE feed_id = :feedId")
    suspend fun getChannelIdByFeedId(feedId: Long) : Long

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM channel_table")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM channel_table ORDER BY pub_date DESC")
    fun getAll() : Flow<List<Channel>>

}