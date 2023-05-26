package vlad.dima.sales.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "order_product",
    primaryKeys = ["orderId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrderProduct(
    val orderId: Int,
    val productId: String,
    val quantity: Int
)
