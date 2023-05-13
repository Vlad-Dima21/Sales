package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
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
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

        val clientId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("clientId", String::class.java)
        } else {
            intent.getSerializableExtra("clientId") as String
        }!!

        val orderId: Int? = intent.getIntExtra("orderId", -1).let {
            if (it == -1) null else it
        }

        val repository = SalesDatabase.getDatabase(this).run {
            OrderRepository(orderDao(), orderProductDao())
        }

        viewModel = ViewModelProvider(
            this, PendingOrderViewModel.Factory(
                clientId, orderId, repository
            )
        )[PendingOrderViewModel::class.java]

        lifecycleScope.launch {
            viewModel.orderStatus.collect { orderStatus ->
                if (orderStatus == PendingOrderViewModel.OrderStatus.SUCCEEDED) {
                    setResult(RESULT_OK, Intent().putExtra("clientId", clientId))
                    finish()
                }
            }
        }

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
                Column(
                    modifier = Modifier.background(MaterialTheme.colors.background)
                ) {
                    PendingOrderAppBar(
                        viewModel = viewModel, isCollapsed = isAppBarCollapsed
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        item { Spacer(modifier = Modifier) }
                        if (isLoading) {
                            item {
                                CircularProgressIndicator()
                            }
                        }
                        items(items = products, key = { item: PendingOrderViewModel.ProductItemHolder -> item.product.productCode }) {
                            ProductItem(productItemHolder = it, modifier = Modifier.animateItemPlacement(
                                animationSpec = tween(600)
                            ))
                        }
                        item { Spacer(modifier = Modifier) }
                    }
                }
            }
        }
    }
}