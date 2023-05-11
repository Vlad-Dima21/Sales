package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import vlad.dima.sales.ui.dashboard.common.products.Product

data class SaleProduct(
    val product: Product,
    val amountSold: Int
)
