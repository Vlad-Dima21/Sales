package vlad.dima.sales.ui.Dashboard.SalesmanDashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.R
import vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Notifications.SalesmanNotificationsAppBar

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