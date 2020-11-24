package com.blogspot.fdbozzo.lectorfeedsrss.database.feed

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Define los métodos para usar la clase Feed con Room.
 */
@Dao
interface FeedDao {

    @Insert
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
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key feed a buscar
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    suspend fun get(key: Long): Feed

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_table")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por linkName.
     */
    @Query("SELECT * FROM feed_table ORDER BY link_name ASC")
    fun getAllFeeds(): LiveData<List<Feed>>

    /**
     * Selecciona y retorna el último feed.
     */
    @Query("SELECT * FROM feed_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastFeed(): Feed?

    /**
     * Selecciona y retorna el feed con el Id indicado.
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    fun getFeedWithId(key: Long): LiveData<Feed>

    /**
     * Selecciona y retorna el feed con el link indicado.
     */
    @Query("SELECT * from feed_table WHERE link = :link")
    fun getFeedWithLink(link: String): LiveData<Feed>

    /**
     * Selecciona y retorna el feed con el linkName indicado.
     */
    @Query("SELECT * from feed_table WHERE link_name = :linkName")
    fun getFeedWithLinkName(linkName: String): LiveData<Feed>
}
