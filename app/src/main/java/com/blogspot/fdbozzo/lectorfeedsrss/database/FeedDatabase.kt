package com.blogspot.fdbozzo.lectorfeedsrss.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.Group
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.FeedDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.FeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.FeedChannelDao
import com.blogspot.fdbozzo.lectorfeedsrss.database.group.GroupDao
import com.blogspot.fdbozzo.lectorfeedsrss.util.DateConverter

/**
 * Base de datos que guarda información de feeds
 * y un método global para acceder a los datos de la misma.
 */
@Database(entities = [Group::class, Feed::class, FeedChannel::class, FeedChannelItem::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class FeedDatabase : RoomDatabase() {

    /**
     * Conecta la base de datos al DAO.
     */
    abstract fun getGroupDao(): GroupDao
    abstract fun getFeedDao(): FeedDao
    abstract fun getFeedChannelDao(): FeedChannelDao
    abstract fun getFeedChannelItemDao(): FeedChannelItemDao

    /**
     * Define el objeto companion, esto nos permite agregar funciones  en la clase FeedDatabase.
     *
     * Por ejemplo, los clientes pueden llamar a `FeedDatabase.getInstance(context)` para instanciar
     * una nueva FeedDatabase.
     */
    companion object {
        /**
         * INSTANCE mantendrá una referencia a cualquier base de datos retornada con getInstance.
         *
         * Esto ayuda a impedir inicializaciones repetidas de la base de datos, que es costoso.
         */
        @Volatile
        private var INSTANCE: FeedDatabase? = null

        /**
         * Función de ayuda para obtener la base de datos mediante singletone.
         *
         * @param context The application context Singleton, used to get access to the filesystem.
         */
        fun getInstance(context: Context): FeedDatabase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {

                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE

                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FeedDatabase::class.java,
                        "feed_database"
                    )
                        // Wipes and rebuilds instead of migrating if no Migration object.
                        // Migration is not part of this lesson. You can learn more about
                        // migration with Room in this blog post:
                        // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                        .fallbackToDestructiveMigration()
                        .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }

                // Return instance; smart cast to be non-null.
                return instance
            }
        }
    }
}
