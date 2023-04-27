package vlad.dima.sales.ui.dashboard.common.products

data class Product(
    var productCode: String = "",
    var productName: String = "",
    var quantitySold: Int = 0,
    var price: Float = 0f
)
