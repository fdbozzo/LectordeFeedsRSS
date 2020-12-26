package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import kotlin.test.*
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExperimentalCoroutinesApi
class FeedChannelItemDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var feedChannelDao: FeedChannelDao
    private lateinit var feedChannelItemDao: FeedChannelItemDao
    private lateinit var db: FeedDatabase
    private lateinit var repository: FeedRepository
    val feed_0 = Feed(groupId = 0, linkName = "HardZone", link = "https://hardzone.es")
    val channel_0 = FeedChannel(feedId = 0, title = "titulo", description = "description", link = "https://hardzone.es")
    val content_0 = FeedChannelItem(feedId = 0, title = "title", link = "https://hardzone.es", description = "description", pubDate = Date())

    @Before
    fun createDb() {
        //val context = InstrumentationRegistry.getInstrumentation().targetContext
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, FeedDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        groupDao = db.getGroupDao()
        feedDao = db.getFeedDao()
        feedChannelDao = db.getFeedChannelDao()
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
        val gId = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        val feed = feed_0.copy(groupId = gId)
        val fId = feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed() ?: throw Exception("lastFeed es null")

        // Inserto un Channel
        //val channel = channel_0.copy(feedId = fId)
        //val channelId = feedChannelDao.insert(channel)

        // Ahora inserto un content (item)
        val content = content_0.copy(feedId = fId)
        feedChannelItemDao.insert(content)
        val lastContent = feedChannelItemDao.getLastFeedItem() ?: throw Exception("lastContent es null")

        Assert.assertEquals("title", lastContent.title)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnContentYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        assertFailsWith<SQLiteConstraintException> {
            // Inserto un Item sin feedId
            val content = content_0.copy(pubDate = Date())
            feedChannelItemDao.insert(content)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarDosContenidosYObtenerUnRecuentoDe_2(): Unit = runBlocking {
        /** Inserta 2 contenidos **/

        // Inserto un grupo
        val group = Group()
        val gId1 = groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        val feed = feed_0.copy(groupId = gId1)
        val fId1 = feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed() ?: throw Exception("lastFeed es null")

        // Ahora inserto un contentEncoded

        // contenido 1
        var content = content_0.copy(feedId = fId1)
        val fcId1 = feedChannelItemDao.insert(content)

        // contenido 2
        content = content_0.copy(feedId = fId1, link = "https://mozilla.org")
        val fcId2 = feedChannelItemDao.insert(content)

        // Obtener toods
        val allContent = feedChannelItemDao.getAllFeedChannelItems() // LiveData<List<FeedChannelItem>>
        //val size = getValue(allContent).size

        //Assert.assertEquals(2, getValue(allContent).size)

        //val valores = allContent.take(2).toList()
        val valores = getValue(feedChannelItemDao.getAllFeedChannelItems().asLiveData())

        Assert.assertEquals(2, valores.size)
    }

}