package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import vlad.dima.sales.room.order.Order
import vlad.dima.sales.ui.dashboard.common.products.Product

data class SaleOrder(
    val order: Order,
    val hasMissingProducts: Boolean,
    val products: List<SaleProduct>
)
