package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import vlad.dima.sales.ui.theme.extra

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    val isHintHidden = viewModel.isHintHidden
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingClients by viewModel.pendingClients.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val invalidStockOrders by viewModel.ordersWithInsufficientStock.collectAsState()
    val invalidProductsOrders by viewModel.ordersWithRemovedProducts.collectAsState()
    val dialogInteractionSource = remember {
        MutableInteractionSource()
    }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val isFabExtended by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset == 0 && lazyListState.firstVisibleItemIndex == 0
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
            if (pendingClients.isNotEmpty() && uploadState == SalesmanPastSalesViewModel.UploadSaleState.Idle) {
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
                            viewModel.placeLocalOrders()
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
            AnimatedVisibility(
                visible = uploadState != SalesmanPastSalesViewModel.UploadSaleState.Idle,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -30 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 30 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clickable(
                            interactionSource = dialogInteractionSource,
                            indication = null
                        ) {
                            viewModel.dismissAlert()
                        }
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (uploadState) {
                                SalesmanPastSalesViewModel.UploadSaleState.Loading -> {
                                    Row(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(width = 8.dp))
                                        Text(
                                            text = stringResource(id = R.string.OrdersUploading)
                                        )
                                    }
                                }

                                SalesmanPastSalesViewModel.UploadSaleState.StockInvalid -> {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = stringResource(id = R.string.Warning),
                                                tint = MaterialTheme.colors.extra
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(id = R.string.OrdersStockInvalid)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        invalidStockOrders.forEach {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Divider(
                                                    Modifier
                                                        .width(2.dp)
                                                        .height(2.dp)
                                                        .background(MaterialTheme.colors.onSurface)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = stringResource(
                                                        id = R.string.OrderNumber,
                                                        it
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                SalesmanPastSalesViewModel.UploadSaleState.ProductsInvalid -> {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = stringResource(id = R.string.Warning),
                                                tint = MaterialTheme.colors.extra
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(id = R.string.OrdersProductsInvalid)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        invalidProductsOrders.forEach {
                                            Row(
                                                modifier = Modifier.height(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Divider(
                                                    Modifier
                                                        .width(2.dp)
                                                        .height(2.dp)
                                                        .background(MaterialTheme.colors.onSurface)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = stringResource(
                                                        id = R.string.OrderNumber,
                                                        it
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                SalesmanPastSalesViewModel.UploadSaleState.UploadSuccessful -> {
                                    Row(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = stringResource(id = R.string.OrdersUploaded),
                                            tint = Color.Green
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(id = R.string.OrdersUploaded)
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}