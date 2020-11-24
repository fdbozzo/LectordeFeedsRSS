package com.blogspot.fdbozzo.lectorfeedsrss.database.content

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Define los métodos para usar la clase Content con Room.
 */
@Dao
interface ContentDao {

    @Insert
    suspend fun insert(content: Content): Long

    /**
     * Cuando se actualiza una fila con un valor existente en la columna,
     * se reemplaza el valor antiguo por el nuevo.
     *
     * @param content nuevo valor a escribir
     */
    @Update
    suspend fun update(content: Content): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key Id del Content a buscar
     */
    @Query("SELECT * from content_table WHERE id = :key")
    suspend fun get(key: Long): Content

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM content_table")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla no leídos,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM content_table WHERE read = 0 ORDER BY pub_date DESC")
    fun getAllContents(): LiveData<List<Content>>

    /**
     * Selecciona y retorna el último content.
     */
    @Query("SELECT * FROM content_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastContent(): Content?

    /**
     * Selecciona y retorna el content con el Id indicado.
     */
    @Query("SELECT * from content_table WHERE id = :key")
    fun getContentWithId(key: Long): LiveData<Content>

    /**
     * Selecciona y retorna el content con el link indicado.
     */
    @Query("SELECT * from content_table WHERE link = :link")
    fun getContentWithLink(link: String): LiveData<Content>
}
