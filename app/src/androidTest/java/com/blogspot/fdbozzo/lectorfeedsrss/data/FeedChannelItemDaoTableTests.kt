package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
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
class FeedChannelItemDaoTableTests {

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
        groupDao.insert(group)
        val lastGroup = groupDao.getLastGroup() ?: throw Exception("lastGroup es null")

        // Inserto un Feed
        val feed = Feed()
        feed.groupId = lastGroup.id
        feedDao.insert(feed)
        val lastFeed = feedDao.getLastFeed() ?: throw Exception("lastFeed es null")

        // Ahora inserto un content

        // contenido 1
        var content = FeedChannelItem()
        content.feedId = lastFeed.id
        feedChannelItemDao.insert(content)

        // contenido 2
        content = FeedChannelItem()
        content.feedId = lastFeed.id
        feedChannelItemDao.insert(content)

        // Obtener toods
        val allContent = feedChannelItemDao.getAllFeedChannelItems() // LiveData<List<FeedChannelItem>>

        Assert.assertEquals(2, getValue(allContent).size)
    }

}