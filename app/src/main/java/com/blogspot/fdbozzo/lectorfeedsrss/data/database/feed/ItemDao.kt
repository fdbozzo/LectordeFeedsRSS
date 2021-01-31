package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase Item con Room.
 */
@Dao
interface ItemDao {

    /**
     *
     * @param item nuevo valor a insertar
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(items: List<Item>)

    /**
     *
     * @param item nuevo valor a reemplazar
     */
    @Update
    suspend fun update(item: Item): Int

    /**
     * Actualiza la marca de read del Item indicado
     */
    @Query("UPDATE item_table SET read = :read WHERE id = :id")
    suspend fun updateReadStatus(id: Long, read: Int): Int

    /**
     * Actualiza la marca de read_later del Item indicado
     */
    @Query("UPDATE item_table SET read_later = :readLater WHERE id = :id")
    suspend fun updateReadLaterStatus(id: Long, readLater: Int): Int

    @Query("UPDATE item_table SET read_later = (1 - read_later) WHERE id = :id")
    suspend fun updateInverseReadLaterStatus(id: Long): Int

    /**
     * Actualiza la marca de leido de todos los Feeds
     */
    @Query("UPDATE item_table SET read = 1")
    suspend fun updateMarkAllFeedAsRead(): Int

    /**
     * Actualiza la marca de leido del Feed indicado
     */
    @Query(
        """UPDATE item_table
        SET read = 1
        WHERE item_table.feed_id IN (
            SELECT fcit.feed_id FROM item_table fcit
            INNER JOIN feed_table ft ON fcit.feed_id = ft.id
            INNER JOIN group_table gt ON ft.group_id = gt.id
            WHERE gt.id = :gropId
        )"""
    )
    suspend fun updateGroupFeedReadStatus(gropId: Long): Int

    /**
     *
     * @param key Id del Item a buscar
     */
    @Query("SELECT * from item_table WHERE id = :key")
    suspend fun get(key: Long): Item

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM item_table")
    suspend fun clear()

    @Query("SELECT COUNT(id) FROM item_table")
    suspend fun itemCount(): Int

    /**
     * Selecciona y retorna todos los datos de la tabla no leídos,
     * ordenados por fecha de publicación descendente.
     */
    @Query("SELECT * FROM item_table WHERE read = 0 ORDER BY pub_date DESC")
    fun getAllItems(): Flow<List<Item>>

    /**
     * Selecciona y retorna todos los datos de la tabla filtrados y
     * ordenados por fecha de publicación descendente.
     */
    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.read <= :read
        AND fcit.read_later >= :readLater
        AND ft.favorite >= :favorite
        AND ft.link_name LIKE :linkName
        ORDER BY fcit.pub_date DESC"""
    )
    fun getFilteredItemsWithFeed(
        linkName: String,
        favorite: Int,
        readLater: Int,
        read: Int
    ): Flow<List<ItemWithFeed>>

    /**
     * Devuelve el item del id indicado e información extra sobre el feed del mismo
     */
    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.id = :id"""
    )
    suspend fun getItemWithFeed(id: Long): ItemWithFeed?

    @Query(
        """SELECT ft.link_name,fcit.* 
        FROM item_table fcit 
        INNER JOIN feed_table ft ON fcit.feed_id = ft.id 
        WHERE fcit.id = :id"""
    )
    fun getItemWithFeedFlow(id: Long): Flow<ItemWithFeed>

    /**
     * Selecciona y retorna el último item.
     */
    @Query("SELECT * FROM item_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastItem(): Item?

}
