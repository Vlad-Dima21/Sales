package vlad.dima.sales.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.room.order.OrderDao
import vlad.dima.sales.model.OrderProduct
import vlad.dima.sales.model.room.order.OrderProductDao
import vlad.dima.sales.model.User
import vlad.dima.sales.model.room.user.UserDao

@Database(
    entities = [User::class, Order::class, OrderProduct::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SalesDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun orderDao(): OrderDao
    abstract fun orderProductDao(): OrderProductDao

    companion object {
        @Volatile
        private var instance: SalesDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE `order`(
                        `orderId` INTEGER PRIMARY KEY NOT NULL,
                        `clientId` TEXT NOT NULL,
                        `salesmanUID` TEXT NOT NULL,
                        `createdDate` INTEGER NOT NULL,
                        FOREIGN KEY (salesmanUID) REFERENCES user(userUID)
                    )
                """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE `order_product`(
                        `orderId` INTEGER NOT NULL,
                        `productId` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        PRIMARY KEY(orderId, productId),
                        FOREIGN KEY(orderId) REFERENCES `order`(orderId) ON DELETE CASCADE
                    )
                """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                        ALTER TABLE `order` ADD COLUMN `total` REAL NOT NULL
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE user ADD COLUMN `email` TEXT NOT NULL"
                )
            }
        }

        fun getDatabase(context: Context): SalesDatabase {
            if (instance != null) {
                return instance as SalesDatabase
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalesDatabase::class.java,
                    "sales_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                Companion.instance = instance
                return instance
            }
        }
    }
}