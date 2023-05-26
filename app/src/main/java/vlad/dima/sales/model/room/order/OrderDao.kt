package vlad.dima.sales.model.room.order

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vlad.dima.sales.model.Order

@Dao
interface OrderDao {
    @Query("SELECT * FROM `order`")
    fun getAll(): Flow<List<Order>>

    @Query("SELECT * FROM `order` WHERE orderId = :id")
    fun getById(id: Int): Flow<Order>

    @Query("SELECT * FROM `order` WHERE salesmanUID = :uid ORDER BY createdDate DESC")
    fun getBySalesmanUID(uid: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg orders: Order): List<Long>

    @Delete
    suspend fun delete(vararg orders: Order)
}