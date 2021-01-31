package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.utilities.CoroutinesTestRule
import com.blogspot.fdbozzo.lectorfeedsrss.util.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import timber.log.Timber
import kotlin.test.*
import java.io.IOException
import java.util.*

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(AndroidJUnit4ClassRunner::class)
@ExperimentalCoroutinesApi
class ItemDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()


    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var channelDao: ChannelDao
    private lateinit var itemDao: ItemDao
    private lateinit var db: FeedDatabase
    private lateinit var repository: FeedRepository
    val feed_0 = Feed(groupId = 0, linkName = "HardZone", link = "https://hardzone.es")
    val channel_0 = Channel(feedId = 0, title = "titulo", description = "description", link = "https://hardzone.es")
    val content_0 = Item(feedId = 0, title = "title", link = "https://hardzone.es", description = "description", pubDate = Date())
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    //val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun createDb() {

        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, FeedDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        groupDao = db.getGroupDao()
        feedDao = db.getFeedDao()
        channelDao = db.getChannelDao()
        itemDao = db.getItemDao()
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
        //val channelId = channelDao.insert(channel)

        // Ahora inserto un content (item)
        val content = content_0.copy(feedId = fId)
        itemDao.insert(content)
        val lastContent = itemDao.getLastItem() ?: throw Exception("lastContent es null")

        Assert.assertEquals("title", lastContent.title)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnContentYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        /**
         * El error de constraint es porque no se indica el feedId
         */
        assertFailsWith<SQLiteConstraintException> {
            // Inserto un Item sin feedId
            val content = content_0.copy(pubDate = Date())
            itemDao.insert(content)
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
        val fcId1 = itemDao.insert(content)

        // contenido 2
        content = content_0.copy(feedId = fId1, link = "https://mozilla.org")
        val fcId2 = itemDao.insert(content)

        // Test
        val valores = getValue(itemDao.getAllItems().asLiveData())

        // Verify
        Assert.assertEquals(2, valores.size)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnGrupoYUnFeedYUnContenido_BorrarElGrupoYObtenerUnRecuentoDe_0_Contenidos(): Unit = runBlocking {
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
        val fcId1 = itemDao.insert(content)

        // contenido 2
        content = content_0.copy(feedId = fId1, link = "https://mozilla.org")
        val fcId2 = itemDao.insert(content)

        // Borro el grupo
        val ret = groupDao.delete(lastGroup)
        Timber.d("groupDao.delete(group) = $ret", ret)

        // Test
        val valores = getValue(itemDao.getAllItems().asLiveData())

        // Verify
        Assert.assertEquals(0, valores.size)
    }

}