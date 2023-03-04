package vlad.dima.sales.ui.Dashboard.SalesmanDashboard.PastSales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R

@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            elevation = 10.dp,
            color = MaterialTheme.colors.primary
        ) {
            Text(
                text = context.getString(R.string.DashboardSales),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}