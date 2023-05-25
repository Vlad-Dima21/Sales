package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager

@Composable
fun SalesmanClientsPage(viewModel: SalesmanClientsViewModel) {
    val clients by viewModel.clients.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        SalesmanClientsAppBar(viewModel)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier)
                }
                items(
                    items = clients,
                    key = { it.client.clientId }
                ) { clientWithInfo ->
                    ClientCard(
                        client = clientWithInfo.client,
                        numberOfSales = clientWithInfo.numberOfSales,
                        isExpanded = viewModel.expandedClient == clientWithInfo.client,
                        onClick = {
                            if (viewModel.expandedClient != clientWithInfo.client) {
                                viewModel.expandedClient = clientWithInfo.client
                            } else if (networkStatus == NetworkManager.NetworkStatus.Available) {
                                viewModel.startCreatingOrder(clientWithInfo.client)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier)
                }
            }
            if (clients.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.NoClients), color = MaterialTheme.colors.onBackground)
                }
            }
        }
    }
}