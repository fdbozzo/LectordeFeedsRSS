package com.blogspot.fdbozzo.lectorfeedsrss.data

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
//import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.blogspot.fdbozzo.lectorfeedsrss.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.database.content.Content
import com.blogspot.fdbozzo.lectorfeedsrss.database.content.ContentDao
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
class ContentDaoTableTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var contentDao: ContentDao
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
        groupDao = db.groupDao
        feedDao = db.feedDao
        contentDao = db.contentDao
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
        val content = Content()
        content.title = "title"
        content.feedId = lastFeed.id
        contentDao.insert(content)
        val lastContent = contentDao.getLastContent() ?: throw Exception("lastContent es null")

        Assert.assertEquals("title", lastContent.title)
    }

    @Test
    @Throws(Exception::class)
    fun deberiaInsertarUnContentYObtenerUnError_SQLiteConstraintException(): Unit = runBlocking {
        assertFailsWith<SQLiteConstraintException> {
            // Inserto un Feed
            val content = Content()
            contentDao.insert(content)
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
        var content = Content()
        content.feedId = lastFeed.id
        contentDao.insert(content)

        // contenido 2
        content = Content()
        content.feedId = lastFeed.id
        contentDao.insert(content)

        // Obtener toods
        val allContent = contentDao.getAllContents() // LiveData<List<Content>>

        Assert.assertEquals(2, getValue(allContent).size)
    }

}