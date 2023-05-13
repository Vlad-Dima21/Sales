package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order.PendingOrderActivity
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy.OrderContextOption
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy.SaleClient

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingClients by viewModel.pendingClients.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .background(MaterialTheme.colors.background)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SalesmanPastSalesAppBar()
            }
            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            if (pendingClients.isNotEmpty()) {
                stickyHeader {
                    Text(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = stringResource(id = R.string.PendingOrders),
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
                items(
                    items = pendingClients,
                    key = { it.client.clientId }
                ) {
                    SaleClient(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .animateItemPlacement(),
                        saleClient = it,
                        onOrderClick = { clientId, orderId ->
                            context.startActivity(
                                Intent(context, PendingOrderActivity::class.java)
                                    .putExtra("clientId", clientId)
                                    .putExtra("orderId", orderId)
                            )
                        },
                        onOrderOptionClick = { option, order ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = when (option) {
                                        OrderContextOption.Delete -> context.getString(
                                            R.string.OrderDeleted,
                                            order.orderId
                                        ).also { viewModel.falseDeleteOrder(order) }
                                    },
                                    actionLabel = context.getString(R.string.Undo)
                                ).let { result ->
                                    when (result) {
                                        SnackbarResult.Dismissed -> viewModel.deleteOrder(order)
                                        SnackbarResult.ActionPerformed -> viewModel.undoFalseDelete(
                                            order
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

        }
    }
}