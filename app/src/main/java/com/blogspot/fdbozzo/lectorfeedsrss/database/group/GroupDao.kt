package com.blogspot.fdbozzo.lectorfeedsrss.database.group

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Define los métodos para usar la clase Group con Room.
 */
@Dao
interface GroupDao {

    @Insert
    suspend fun insert(group: Group): Long

    /**
     * Actualiza el group indicado
     *
     * @param group nuevo valor a escribir
     */
    @Update
    suspend fun update(group: Group): Int

    /**
     * Selecciona y retorna la fila que coincide con la clave indicada
     *
     * @param key Group a buscar
     */
    @Query("SELECT * from group_table WHERE id = :key")
    suspend fun get(key: Long): Group

    /**
     * Borra todos los datos de la tabla
     */
    @Query("DELETE FROM group_table")
    suspend fun clear()

    /**
     * Selecciona y retorna todos los datos de la tabla,
     * ordenados por groupName.
     */
    @Query("SELECT * FROM group_table ORDER BY group_name ASC")
    fun getAllGroups(): LiveData<List<Group>>

    /**
     * Selecciona y retorna el último Group.
     */
    @Query("SELECT * FROM group_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastGroup(): Group?

    /**
     * Selecciona y retorna el Group con el Id indicado.
     */
    @Query("SELECT * from group_table WHERE id = :key")
    fun getGroupWithId(key: Long): LiveData<Group>

    /**
     * Selecciona y retorna el Group con el link indicado.
     */
    @Query("SELECT * from group_table WHERE group_name = :groupName")
    fun getGroupWithName(groupName: String): LiveData<Group>
}
