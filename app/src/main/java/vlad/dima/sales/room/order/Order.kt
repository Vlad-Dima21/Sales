package vlad.dima.sales.room.order

import androidx.room.*
import vlad.dima.sales.room.user.User
import java.util.Date

@Entity(
    tableName = "order",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userUID"],
        childColumns = ["salesmanUID"]
    )],
    ignoredColumns = ["products"]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    var orderId: Int = 0,
    var clientId: String = "",
    var salesmanUID: String = "",
    var products: Map<String, Int> = mapOf(),
    var total: Float = 0f,
    var createdDate: Date = Date()
)