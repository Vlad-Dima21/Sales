package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import vlad.dima.sales.ui.composables.AsyncImage
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
                val isAppBarCollapsed by remember {
                    derivedStateOf {
                        scrollState.firstVisibleItemIndex != 0 || scrollState.firstVisibleItemScrollOffset != 0
                    }
                }
                var viewedImage by rememberSaveable {
                    mutableStateOf(Uri.EMPTY)
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
                        items(
                            items = products,
                            key = { item: PendingOrderViewModel.ProductItemHolder -> item.product.productCode }) {
                            ProductItem(
                                productItemHolder = it,
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = tween(600)
                                ),
                                onImageClick = { imageUri ->
                                    viewedImage = imageUri
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier) }
                        item { Spacer(modifier = Modifier) }
                    }
                }
                AnimatedVisibility(
                    visible = viewedImage != Uri.EMPTY,
                    enter = fadeIn(),
                    exit = ExitTransition.None
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colors.background.copy(.8f)
                            )
                            .clickable(
                                MutableInteractionSource(),
                                indication = null
                            ) {
                                viewedImage = Uri.EMPTY
                            }
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(.6f)
                                .align(Alignment.Center),
                            imageUri = viewedImage,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            loading = { CircularProgressIndicator() }
                        )
                    }
                }
            }
        }
    }
}