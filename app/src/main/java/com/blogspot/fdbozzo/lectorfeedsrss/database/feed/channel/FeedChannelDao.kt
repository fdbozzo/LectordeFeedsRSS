package com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel

import androidx.lifecycle.LiveData
import androidx.room.*

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
    fun insert(feedChannel: FeedChannel) : Long

    /**
     *
     * @param feedItem nuevo valor a insertar
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feeds: List<FeedChannel>)

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
    @Query("SELECT * FROM feed_channel WHERE title = :title")
    fun get(title: String) : LiveData<FeedChannel>

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_channel")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM feed_channel ORDER BY pub_date DESC")
    fun getAll() : LiveData<List<FeedChannel>>

}