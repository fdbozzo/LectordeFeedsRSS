package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.getValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
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
    fun deberiaInsertarUnGrupoVacioYObtenerElNombrePorDefecto_other() = runBlocking {
        val group = Group()
        val insId1 = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup()
        Assert.assertEquals("other", lastGroup?.groupName)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoVacioPorDuplicadoYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        /** El error es porque hay un índice que impide nombres duplicados **/
        assertFailsWith<SQLiteConstraintException> {
            // grupo 1
            var group = Group()
            val insId1 = groupDao.insert(group)
            // grupo 2
            group = Group()
            val insId2 = groupDao.insert(group)
        }
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
        val allGroups = groupDao.getAllGroups()
        //val size = getValue(allGroups).size

        // Verify
        //Assert.assertEquals(2, getValue(allGroups).size)
        val elements = allGroups.take(1).toList()
        Assert.assertEquals(2, elements[0].size)

    }


}