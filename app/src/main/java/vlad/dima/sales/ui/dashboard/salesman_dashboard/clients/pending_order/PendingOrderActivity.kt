package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.OrderRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.ui.composables.AsyncImage
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

        val networkManager = NetworkManager(applicationContext)

        viewModel = ViewModelProvider(
            this, PendingOrderViewModel.Factory(
                clientId, orderId, repository, networkManager
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
                val networkStatus by networkManager.currentConnection.collectAsState(NetworkManager.NetworkStatus.Available)
                val systemBarsColor by animateColorAsState(
                    if (networkStatus != NetworkManager.NetworkStatus.Available) MaterialTheme.colors.error else MaterialTheme.colors.background,
                    label = "statusBarColor"
                )
                val uiController = rememberSystemUiController()
                uiController.setStatusBarColor(systemBarsColor)
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
                var isImageViewed by rememberSaveable {
                    mutableStateOf(false)
                }
                Column(
                    modifier = Modifier.background(MaterialTheme.colors.background)
                ) {
                    AnimatedVisibility(visible = networkStatus != NetworkManager.NetworkStatus.Available) {
                        Text(
                            text = stringResource(id = R.string.CheckConnection),
                            color = MaterialTheme.colors.onError,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(systemBarsColor)
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
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
                            .imePadding()
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
                                    animationSpec = tween(400)
                                ),
                                onImageClick = { imageUri ->
                                    viewedImage = imageUri
                                    isImageViewed = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier) }
                        item { Spacer(modifier = Modifier) }
                    }
                }
                AnimatedVisibility(
                    visible = isImageViewed,
                    enter = fadeIn(),
                    exit = fadeOut()
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
                                isImageViewed = false
                            }
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(.6f)
                                .align(Alignment.Center),
                            imageUri = viewedImage,
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            loading = { CircularProgressIndicator() }
                        )
                    }
                }
            }
        }
    }
}