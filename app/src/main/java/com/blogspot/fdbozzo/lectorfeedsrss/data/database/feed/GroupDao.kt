package com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Define los métodos para usar la clase Group con Room.
 */
@Dao
interface GroupDao {

    @Insert
    fun insert(group: Group): Long

    /**
     * Actualiza el group indicado
     *
     * @param group nuevo valor a escribir
     */
    @Update
    fun update(group: Group): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key Group a buscar
     */
    @Query("SELECT * from group_table WHERE id = :key")
    fun get(key: Long): Group

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key Group a buscar
     */
    @Query("SELECT id from group_table WHERE group_name = :key")
    fun get(key: String): Long

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM group_table")
    fun clear()

    @Query("SELECT COUNT(id) FROM group_table")
    fun groupCount(): Int

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por groupName.
     */
    @Query("SELECT * FROM group_table ORDER BY group_name ASC")
    fun getAllGroups(): Flow<List<Group>>

    /**
     * Selecciona y retorna el último Group.
     */
    @Query("SELECT * FROM group_table ORDER BY id DESC LIMIT 1")
    fun getLastGroup(): Group?

    /**
     * Selecciona y retorna el Group con el Id indicado.
     */
    @Query("SELECT * from group_table WHERE id = :key")
    fun getGroupWithId(key: Long): Group

    /**
     * Selecciona y retorna el Group con el link indicado.
     */
    @Query("SELECT * from group_table WHERE group_name = :groupName")
    fun getGroupWithName(groupName: String): Group
}
