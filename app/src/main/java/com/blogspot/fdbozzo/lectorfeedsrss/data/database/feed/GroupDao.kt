package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase Group con Room.
 */
@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(group: Group): Long

    /**
     * Actualiza el group indicado
     *
     * @param group nuevo valor a escribir
     */
    @Update
    suspend fun update(group: Group): Int

    @Delete
    suspend fun delete(group: Group): Int

    @Query("DELETE FROM group_table")
    suspend fun deleteAll(): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key Group a buscar
     */
    @Query("SELECT * from group_table WHERE id = :key")
    suspend fun getGroupById(key: Long): Group?

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param name Group a buscar
     */
    @Query("SELECT id from group_table WHERE group_name = :name")
    suspend fun getGroupIdByName(name: String): Long?

    @Query("SELECT COUNT(id) FROM group_table")
    suspend fun groupCount(): Long

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por groupName.
     */
    @Query("SELECT * FROM group_table ORDER BY group_name ASC")
    fun getAllGroupsFlow(): Flow<List<Group>?>

    @Query("SELECT * FROM group_table ORDER BY group_name ASC")
    fun getAllGroups(): List<Group>?

    /**
     * Selecciona y retorna el último Group.
     */
    @Query("SELECT * FROM group_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastGroup(): Group?

    /**
     * Selecciona y retorna el Group con el link indicado.
     */
    @Query("SELECT * from group_table WHERE group_name = :groupName")
    suspend fun getGroupByName(groupName: String): Group?

    /**
     * Esta función se usa para los items (grupo / feed) del menú del Drawer
     */
    @Transaction
    @Query("select * from group_table ORDER BY group_name")
    suspend fun getGroupsWithFeeds(): List<GroupWithFeeds>?

}
