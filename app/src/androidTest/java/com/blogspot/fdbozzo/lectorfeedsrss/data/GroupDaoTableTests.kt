package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.Group
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.GroupDao
import com.demo.rssfeedreader.utilities.getValue
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.*
import java.io.IOException

//@RunWith(AndroidJUnit4::class)
class GroupDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

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
        groupDao.insert(group)
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
            groupDao.insert(group)
            // grupo 2
            group = Group()
            groupDao.insert(group)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosGruposConDistintoNombreYObtenerUnRecuentoDe_2(): Unit = runBlocking {
        // grupo 1
        var group = Group()
        groupDao.insert(group)
        // grupo 2
        group = Group()
        group.groupName = "otro más"
        groupDao.insert(group)

        val allGroups = groupDao.getAllGroups() // LiveData<List<Group>>

        Assert.assertEquals(2, getValue(allGroups).size)
    }


}