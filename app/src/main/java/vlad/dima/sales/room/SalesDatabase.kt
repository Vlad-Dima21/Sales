package vlad.dima.sales.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vlad.dima.sales.room.user.User
import vlad.dima.sales.room.user.UserDao

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class SalesDatabase: RoomDatabase() {

    abstract fun userDAO(): UserDao

    companion object {
        @Volatile
        private var instance: SalesDatabase? = null

        fun getDatabase(context: Context): SalesDatabase {
            if (instance != null) {
                return instance as SalesDatabase
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalesDatabase::class.java,
                    "sales_database"
                ).build()
                Companion.instance = instance
                return instance
            }
        }
    }
}