package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import com.blogspot.fdbozzo.lectorfeedsrss.util.getValue
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import kotlin.test.*
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
@ExperimentalCoroutinesApi
class GroupDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var feedChannelItemDao: FeedChannelItemDao
    private lateinit var db: FeedDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, FeedDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        groupDao = db.getGroupDao()
        feedDao = db.getFeedDao()
        feedChannelItemDao = db.getFeedChannelItemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoVacioYObtenerElNombrePorDefecto_Uncategorized() = runBlocking {
        val group = Group()
        val insId1 = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup()
        Assert.assertEquals(Group.DEFAULT_NAME, lastGroup?.groupName)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoVacioPorDuplicadoYObtenerUnRetorno_menosUno(): Unit = runBlocking {
        /** En vez de error se devuelve -1 porque hay un índice que impide nombres duplicados **/
        /*
        assertFailsWith<SQLiteConstraintException> {
            // grupo 1
            var group = Group()
            val insId1 = groupDao.insert(group)
            // grupo 2
            group = Group()
            val insId2 = groupDao.insert(group)
        }
         */
        // grupo 1
        var group = Group()
        val insId1 = groupDao.insert(group)
        // grupo 2
        group = Group()
        val insId2 = groupDao.insert(group)

        Assert.assertEquals(-1, insId2)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosGruposConDistintoNombreYObtenerUnRecuentoDe_2(): Unit = runBlocking {
        // grupo 1
        var group = Group()
        val insId1 = groupDao.insert(group)
        // grupo 2
        group = Group()
        group.groupName = "otro más"
        val insId2 = groupDao.insert(group)

        // Test
        val valores = getValue(groupDao.getAllGroupsFlow().asLiveData()) ?: throw Exception("valores es null")

        // Verify
        Assert.assertEquals(2, valores.size)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoYBorrarloYObtenerUnRecuentoDe_0(): Unit = runBlocking {
        // grupo 1
        val group = Group()
        val insVals = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")
        groupDao.delete(lastGroup)

        // Test
        val valores = getValue(groupDao.getAllGroupsFlow().asLiveData()) ?: throw Exception("valores es null")

        // Verify
        Assert.assertEquals(0, valores.size)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosGruposConDistintoNombreYObtenerElIdDelSegundoGrupo(): Unit = runBlocking {
        // grupo 1
        var group = Group()
        val insId1 = groupDao.insert(group)
        // grupo 2
        group = Group()
        group.groupName = "otro más"
        val insId2 = groupDao.insert(group)

        // Test
        group = groupDao.getGroupById(2)?: Group()

        // Verify
        Assert.assertEquals(2, group.id)
    }


}