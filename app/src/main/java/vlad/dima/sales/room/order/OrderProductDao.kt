package vlad.dima.sales.room.order

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderProductDao {

    @Query("SELECT orderId, productId, quantity FROM order_product JOIN `order` USING(orderId) WHERE salesmanUID = :uid")
    fun getBySalesmanUID(uid: String): Flow<List<OrderProduct>>

    @Query("SELECT * from order_product JOIN `order` USING(orderId) WHERE orderId = :orderId")
    fun getByOrderId(orderId: Int): Flow<List<OrderProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderProducts: List<OrderProduct>)

    @Delete
    suspend fun delete(orderProducts: List<OrderProduct>)
}