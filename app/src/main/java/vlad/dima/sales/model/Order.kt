package vlad.dima.sales.model

import androidx.room.*
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