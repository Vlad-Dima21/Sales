package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import vlad.dima.sales.model.Client

data class SaleClient(
    val client: Client,
    val orderList: List<SaleOrder>
)