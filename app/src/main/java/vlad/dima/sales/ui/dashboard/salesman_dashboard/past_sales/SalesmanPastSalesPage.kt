package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order.ProductItem

@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        SalesmanPastSalesAppBar()
        ProductItem(product = Product("", "28A9A", "Set Burghie Spirale", "Set burghie puternice cu tăiş pe dreapta", 5, 0, 20.99f, 100), imageUrl = "")
    }
}