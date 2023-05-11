package vlad.dima.sales.room.order

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderProductDao {

    @Query("SELECT orderId, productId, quantity FROM order_product JOIN `order` USING(orderId) WHERE salesmanUID = :uid")
    fun getBySalesmanUID(uid: String): Flow<List<OrderProduct>>

    @Insert(onConflict = REPLACE)
    fun insert(orderProducts: List<OrderProduct>)

    @Delete
    fun delete(orderProducts: List<OrderProduct>)
}