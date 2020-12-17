package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase Feed con Room.
 */
@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feed: Feed): Long

    /**
     * Cuando se actualiza una fila con un valor existente en la columna,
     * se reemplaza el valor antiguo por el nuevo.
     *
     * @param feed nuevo valor a escribir
     */
    @Update
    fun update(feed: Feed): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key feed a buscar
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    fun get(key: Long): Feed

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param link feed a buscar
     */
    @Query("SELECT id from feed_table WHERE link = :link")
    fun get(link: String): Long

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM feed_table")
    fun clear()

    @Query("SELECT COUNT(id) FROM feed_table")
    fun feedCount(): Int

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
    fun getLastFeed(): Feed?

    /**
     * Selecciona y retorna el feed con el Id indicado.
     */
    @Query("SELECT * from feed_table WHERE id = :key")
    fun getFeedWithId(key: Long): Feed

    /**
     * Selecciona y retorna el feed con el link indicado.
     */
    @Query("SELECT * from feed_table WHERE link = :link")
    fun getFeedWithLink(link: String): Feed

    /**
     * Selecciona y retorna el feed con el linkName indicado.
     */
    @Query("SELECT * from feed_table WHERE link_name = :linkName")
    fun getFeedWithLinkName(linkName: String): Feed
}
