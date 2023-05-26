package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import vlad.dima.sales.model.Product

data class SaleProduct(
    val product: Product,
    val amountSold: Int
)
