package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

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
import vlad.dima.sales.R

@Composable
fun SalesmanClientsAppBar() {
    val localContext = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        color = MaterialTheme.colors.primary
    ) {
        Text(
            text = localContext.getString(R.string.DashboardClients),
            fontSize = 20.sp,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}