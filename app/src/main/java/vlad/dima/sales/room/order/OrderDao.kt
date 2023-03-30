package vlad.dima.sales.room.order

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface OrderDao {
    @Query("SELECT * FROM `order`")
    fun getAll(): LiveData<List<Order>>

    @Query("SELECT * FROM `order` WHERE orderId = :id")
    fun getById(id: Int): LiveData<Order>

    @Query("SELECT * FROM `order` WHERE salesmanUID = :uid ORDER BY createdDate DESC")
    fun getBySalesmanUID(uid: String): LiveData<List<Order>>

    @Insert(onConflict = REPLACE)
    fun insert(vararg orders: Order)

    @Delete
    fun delete(vararg orders: Order)
}