package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R

@Composable
fun SalesmanPastSalesAppBar() {
    val localContext = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f),
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        color = MaterialTheme.colors.primary
    ) {
        Text(
            text = localContext.getString(R.string.DashboardSales),
            fontSize = 20.sp,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}