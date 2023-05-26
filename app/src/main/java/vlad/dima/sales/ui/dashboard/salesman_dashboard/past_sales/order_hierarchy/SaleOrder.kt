package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import vlad.dima.sales.model.Order

data class SaleOrder(
    val order: Order,
    val hasMissingProducts: Boolean,
    val hasInsufficientStock: Boolean,
    val products: List<SaleProduct>
)
