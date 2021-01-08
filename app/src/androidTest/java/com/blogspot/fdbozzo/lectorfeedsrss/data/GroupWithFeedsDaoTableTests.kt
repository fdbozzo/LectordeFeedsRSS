package com.blogspot.fdbozzo.lectorfeedsrss.data

//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.*
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
@ExperimentalCoroutinesApi
class GroupWithFeedsDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    //private lateinit var feedChannelItemDao: FeedChannelItemDao
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
        //feedChannelItemDao = db.getFeedChannelItemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosGruposConDistintoNombre_UnFeedPorGrupoYObtenerUnaListaDeGruposYFeeds(): Unit = runBlocking {
        // grupo 1
        var group = Group() // "Uncategorized"
        val insId1 = groupDao.insert(group)
        // grupo 2
        group = Group()
        group.groupName = "Another group"
        val insId2 = groupDao.insert(group)
        // feed 1, group 1
        var feed = Feed(groupId = insId1, linkName = "HardZone", link = "https://hardzone.es")
        feedDao.insert(feed)
        // feed 2, group 2
        feed = Feed(groupId = insId2, linkName = "Mozilla", link = "http://blog.mozilla.com")
        feedDao.insert(feed)

        // Test
        //val allGroupsWithFeeds = groupDao.getGroupsWithFeeds()
        //val size = getValue(allGroups).size
        val valores = groupDao.getGroupsWithFeeds()

        // Verify
        //Assert.assertEquals(2, getValue(allGroups).size)
        val elements = valores?.toList()
        Assert.assertEquals(2, elements?.size)
        Assert.assertEquals(1, elements?.get(0)?.feeds?.size)
        Assert.assertEquals(1, elements?.get(1)?.feeds?.size)

    }


}