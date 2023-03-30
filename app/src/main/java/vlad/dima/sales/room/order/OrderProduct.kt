package vlad.dima.sales.room.order

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "order_product",
    primaryKeys = ["orderId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = CASCADE
        )
    ]
)
data class OrderProduct(
    val orderId: Int,
    val productId: String,
    val quantity: Int
)
