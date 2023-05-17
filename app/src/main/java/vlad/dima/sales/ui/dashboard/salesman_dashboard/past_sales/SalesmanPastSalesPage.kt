package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    val isHintHidden = viewModel.isHintHidden
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingClients by viewModel.pendingClients.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val isFabExtended by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    contentColor = MaterialTheme.colors.onSurface,
                    actionColor = MaterialTheme.colors.secondary,
                    backgroundColor = MaterialTheme.colors.surface
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column {
                SalesmanPastSalesAppBar(
                    isHintVisible = pendingClients.isNotEmpty() && !isHintHidden,
                    onHintClick = viewModel::hideHint
                )
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = lazyListState
                ) {
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
                                                SnackbarResult.Dismissed -> viewModel.deleteOrder(
                                                    order
                                                )

                                                SnackbarResult.ActionPerformed -> viewModel.undoFalseDelete(
                                                    order
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier) }
                    }

                }
                if (pendingClients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.sizeIn(maxWidth = 100.dp),
                                painter = painterResource(id = R.drawable.empty_box),
                                contentDescription = stringResource(id = R.string.NoOrders),
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onBackground)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.NoOrders),
                                color = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                }
            }
            if (pendingClients.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onSecondary,
                        elevation = dimensionResource(id = R.dimen.standard_elevation),
                        onClick = {
                            //todo
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = stringResource(id = R.string.ConfirmOrders)
                            )
                            AnimatedVisibility(visible = isFabExtended) {
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = stringResource(id = R.string.ConfirmOrders)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}