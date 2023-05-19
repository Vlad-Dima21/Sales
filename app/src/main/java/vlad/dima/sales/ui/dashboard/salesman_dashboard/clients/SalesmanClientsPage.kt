package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SalesmanClientsPage(viewModel: SalesmanClientsViewModel) {
    val clients by viewModel.clients.collectAsState()
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        SalesmanClientsAppBar()
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier)
            }
            items(
                items = clients,
                key = {it.client.clientId}
            ) {clientWithInfo ->
                ClientCard(client = clientWithInfo.client, numberOfSales = clientWithInfo.numberOfSales, viewModel = viewModel)
            }
        }
    }
}