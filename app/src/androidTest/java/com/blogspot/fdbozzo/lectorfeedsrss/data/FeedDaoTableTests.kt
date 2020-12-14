package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import kotlin.test.*
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
@ExperimentalCoroutinesApi
class FeedDaoTableTests {

    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var feedChannelItemDao: FeedChannelItemDao
    private lateinit var db: FeedDatabase

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


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
    fun deberiaInsertarUnGrupoYUnFeedYObtenerUnFeedCon_groupId_igualAl_id_delGrupoCreado() = runBlocking {
        // Inserto primero un grupo
        val group = Group()
        val insGroup = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Ahora inserto un Feed
        val feed = Feed()
        feed.groupId = lastGroup.id
        val insFeed = feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed()

        Assert.assertEquals(lastGroup.id, lastFeed?.groupId)
        Assert.assertEquals(1, insFeed)
        Assert.assertEquals(1, insGroup)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnFeedYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        assertFailsWith<SQLiteConstraintException> {
            // Inserto un Feed
            val feed = Feed()
            feedDao.insert(feed)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoYDosFeedYObtenerDosFeedCon_groupId_igualAl_id_delGrupoCreado() = runBlocking {
        /** Inserta 1 grupo y 2 feed **/
        // Inserto primero un grupo
        val group = Group()
        groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        var feed = Feed()
        feed.groupId = lastGroup.id
        feedDao.insert(feed)
        feedDao.getLastFeed() ?: throw Exception("lastFeed(1) es null")

        // Inserto otro Feed
        feed = Feed()
        feed.groupId = lastGroup.id
        feedDao.insert(feed)
        feedDao.getLastFeed() ?: throw Exception("lastFeed(2) es null")

        // Test
        //val allFeeds = feedDao.getAllFeeds() // LiveData<List<Feed>>
        val allFeeds = feedDao.getAllFeeds() // Flow<List<Feed>>

        // Verify
        val valores = allFeeds.take(1).toList()

        Assert.assertEquals(2, valores[0].size)
    }


}