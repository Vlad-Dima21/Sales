package vlad.dima.sales.ui.Dashboard.SalesmanDashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.R

@Composable
fun SalesmanNotificationsPage(viewModel: SalesmanNotificationsViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = 10.dp,
            color = MaterialTheme.colors.primary
        ) {
            Text(
                text = context.getString(R.string.DashboardNotifications),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
        }

        Button(
            onClick = {
                viewModel.Logout()
            }
        ) {
            Text(text = "Logout")
        }
    }
}