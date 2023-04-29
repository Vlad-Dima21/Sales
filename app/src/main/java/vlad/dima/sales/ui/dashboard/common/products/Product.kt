package vlad.dima.sales.ui.dashboard.common.products

import com.google.firebase.firestore.Exclude

data class Product(
    @Exclude var productId: String = "",
    var productCode: String = "",
    var productName: String = "",
    var productDescription: String = "",
    var quantitySold: Int = 0,
    @Exclude var quantityAdded: Int = 0,
    var price: Float = 0f,
    var stock: Int = 0
)
