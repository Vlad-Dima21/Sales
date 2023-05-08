package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import vlad.dima.sales.ui.theme.SalesTheme

class PendingOrderActivity : ComponentActivity() {

    private lateinit var viewModel: PendingOrderViewModel

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("client", Client::class.java)
        } else {
            intent.getSerializableExtra("client") as Client
        }!!

        val repository = SalesDatabase.getDatabase(this).run {
            OrderRepository(orderDao(), orderProductDao())
        }

        viewModel = ViewModelProvider(
            this,
            PendingOrderViewModel.Factory(
                client,
                repository
            )
        )[PendingOrderViewModel::class.java]

        setContent {
            SalesTheme(
                defaultSystemBarsColor = false
            ) {
                val uiController = rememberSystemUiController()
                uiController.setStatusBarColor(MaterialTheme.colors.background)
                uiController.setNavigationBarColor(MaterialTheme.colors.background)

                val products by viewModel.products.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                val scrollState = rememberLazyListState()
                var previousScrollItem by remember { mutableStateOf(0) }
                val isAppBarCollapsed by remember {
                    derivedStateOf {
                        if (scrollState.firstVisibleItemIndex != 0) {
                            (scrollState.firstVisibleItemIndex > previousScrollItem)
                        } else {
                            false
                        }.also {
                            previousScrollItem = scrollState.firstVisibleItemIndex
                        }
                    }
                }
                Column {
                    PendingOrderAppBar(viewModel = viewModel, client = client, isCollapsed = isAppBarCollapsed)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                    ) {
                        item { Spacer(modifier = Modifier) }
                        if (isLoading) {
                            item {
                                CircularProgressIndicator()
                            }
                        }
                        items(
                            items = products,
                            key = { item: Product -> item.productCode }
                        ) {
                            ProductItem(
                                product = it,
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = tween(600)
                                ),
                                quantityChangedCallback = { oldValue, newValue, product ->
                                    viewModel.updateProductInCart(oldValue, newValue, product)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier) }
                    }
                }
            }
        }
    }
}