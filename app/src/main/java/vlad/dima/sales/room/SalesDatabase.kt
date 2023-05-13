package vlad.dima.sales.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.order.OrderDao
import vlad.dima.sales.room.order.OrderProduct
import vlad.dima.sales.room.order.OrderProductDao
import vlad.dima.sales.room.user.User
import vlad.dima.sales.room.user.UserDao

@Database(
    entities = [User::class, Order::class, OrderProduct::class],
    version = 3,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                Companion.instance = instance
                return instance
            }
        }
    }
}