package vlad.dima.sales.ui.dashboard.salesman_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsAppBar

@Composable
fun SalesmanNotificationsPage(viewModel: SalesmanNotificationsViewModel) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SalesmanNotificationsAppBar() }
        items(
            items = viewModel.sampleData
        ) {
            Text(text = it.toString())
        }
        item {
            Button(
                onClick = {
                    viewModel.Logout()
                }
            ) {
                Text(text = "Logout")
            }
        }
    }
}