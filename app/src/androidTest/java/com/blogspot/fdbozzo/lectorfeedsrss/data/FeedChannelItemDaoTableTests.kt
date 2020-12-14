package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
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
class FeedChannelItemDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var feedChannelItemDao: FeedChannelItemDao
    private lateinit var db: FeedDatabase
    private lateinit var repository: FeedRepository

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
        repository = FeedRepository(RoomDataSource(db), RssFeedDataSource())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoUnFeedYUnContentYObtenerUnContentConElTituloIndicado() = runBlocking {
        // Inserto un grupo
        val group = Group()
        groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        val feed = Feed()
        feed.groupId = lastGroup.id
        feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed() ?: throw Exception("lastFeed es null")

        // Ahora inserto un content
        val content = FeedChannelItem()
        content.title = "title"
        content.feedId = lastFeed.id
        feedChannelItemDao.insert(content)
        val lastContent = feedChannelItemDao.getLastFeedItem() ?: throw Exception("lastContent es null")

        Assert.assertEquals("title", lastContent.title)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnContentYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        assertFailsWith<SQLiteConstraintException> {
            // Inserto un Feed
            val content = FeedChannelItem()
            feedChannelItemDao.insert(content)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosContenidosYObtenerUnRecuentoDe_2(): Unit = runBlocking {
        /** Inserta 2 contenidos **/

        // Inserto un grupo
        val group = Group()
        val insId1 = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        val feed = Feed()
        feed.groupId = lastGroup.id
        val insId2 = feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed() ?: throw Exception("lastFeed es null")

        // Ahora inserto un content

        // contenido 1
        var content = FeedChannelItem()
        content.feedId = lastFeed.id
        val insId3 = feedChannelItemDao.insert(content)

        // contenido 2
        content = FeedChannelItem()
        content.feedId = lastFeed.id
        val insId4 = feedChannelItemDao.insert(content)

        // Obtener toods
        val allContent = feedChannelItemDao.getAllFeedChannelItems() // LiveData<List<FeedChannelItem>>
        //val size = getValue(allContent).size

        //Assert.assertEquals(2, getValue(allContent).size)

        val valores = allContent.take(1).toList()
        Assert.assertEquals(2, valores[0].size)
    }

}