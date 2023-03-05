package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        SalesmanPastSalesAppBar()
    }
}