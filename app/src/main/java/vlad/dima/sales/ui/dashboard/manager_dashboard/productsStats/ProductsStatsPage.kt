package vlad.dima.sales.ui.dashboard.manager_dashboard.productsStats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.composables.AsyncImage
import vlad.dima.sales.ui.composables.Chart
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductsStatsPage(viewModel: ProductsStatsViewModel) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statsInterval by viewModel.statsInterval.collectAsState()
    val selectedProductSelling by viewModel.selectedProductSelling.collectAsState()
    val selectedProductProfitable by viewModel.selectedProductProfitable.collectAsState()
    val topSellingProducts by viewModel.topSellingProducts.collectAsState()
    val topSellingChartData by viewModel.topSellingProductsChartData.collectAsState()
    val topProfitableProducts by viewModel.topProfitableProducts.collectAsState()
    val topProfitableChartData by viewModel.topProfitableChartData.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = viewModel::loadOrdersAndProducts)

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        ProductsStatsAppBar(onSelectInterval = { interval ->
            viewModel.changeInterval(interval)
        })
        Box(
            modifier = Modifier.padding(horizontal = 8.dp)
                .let {
                    if (networkStatus == NetworkManager.NetworkStatus.Available) {
                        return@let it.pullRefresh(pullRefreshState)
                    }
                    it
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (topSellingProducts.isEmpty()) Arrangement.Center else Arrangement.Top
            ) {
                if (topSellingProducts.isEmpty() && topProfitableProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.sizeIn(maxWidth = 80.dp),
                                painter = painterResource(id = R.drawable.empty_box),
                                contentDescription = stringResource(id = R.string.NoProductStatsAvailable),
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onBackground)
                            )
                            Text(
                                text = stringResource(id = R.string.NoProductStatsAvailable),
                                color = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Equalizer,
                                    contentDescription = stringResource(R.string.BestSellingProducts),
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.BestSellingProducts),
                                    color = MaterialTheme.colors.onBackground,
                                    fontSize = 28.sp,
                                )
                            }
                            Divider(
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(.7f)
                            )
                            topSellingProducts.forEachIndexed { index, product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                                        .clickable {
                                            viewModel.setSellingProduct(product)
                                        }
                                        .let {
                                            if (product == selectedProductSelling) {
                                                return@let it.border(
                                                    1.dp,
                                                    MaterialTheme.colors.secondaryVariant,
                                                    RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                                                )
                                            }
                                            return@let it
                                        }
                                        .background(MaterialTheme.colors.surface)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        imageUri = product.productImageUri,
                                        contentDescription = product.productName,
                                        contentScale = ContentScale.Crop,
                                        loading = { CircularProgressIndicator() },
                                        shapeCrop = CircleShape,
                                        size = 70.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${index + 1}. ${product.productName}",
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier)
                            if (selectedProductSelling != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShowChart,
                                        contentDescription = stringResource(R.string.BestSellingProducts),
                                        modifier = Modifier.size(25.dp),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(
                                            id = R.string.SalesForProduct,
                                            selectedProductSelling!!.productName,
                                            run {
                                                val currentDate = Calendar.getInstance()
                                                val xDaysAgo = Calendar.getInstance()
                                                xDaysAgo.add(
                                                    Calendar.DAY_OF_MONTH,
                                                    -(statsInterval - 1)
                                                )
                                                "${
                                                    xDaysAgo.get(Calendar.DAY_OF_MONTH).toString()
                                                        .padStart(2, '0')
                                                }.${
                                                    (xDaysAgo.get(Calendar.MONTH) + 1).toString()
                                                        .padStart(2, '0')
                                                } - ${
                                                    currentDate.get(Calendar.DAY_OF_MONTH)
                                                        .toString().padStart(2, '0')
                                                }.${
                                                    (currentDate.get(Calendar.MONTH) + 1).toString()
                                                        .padStart(2, '0')
                                                }"
                                            }
                                        ),
                                        color = MaterialTheme.colors.onBackground,
                                        fontSize = 16.sp,
                                    )
                                }
                                Chart(
                                    data = topSellingChartData, modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                Spacer(modifier = Modifier)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Equalizer,
                                    contentDescription = stringResource(R.string.MostProfitableProducts),
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.MostProfitableProducts),
                                    color = MaterialTheme.colors.onBackground,
                                    fontSize = 28.sp,
                                )
                            }
                            Divider(
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(.7f)
                            )
                            topProfitableProducts.forEachIndexed { index, product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                                        .clickable {
                                            viewModel.setProfitableProduct(product)
                                        }
                                        .let {
                                            if (product == selectedProductProfitable) {
                                                return@let it.border(
                                                    1.dp,
                                                    MaterialTheme.colors.secondaryVariant,
                                                    RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                                                )
                                            }
                                            return@let it
                                        }
                                        .background(MaterialTheme.colors.surface)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        imageUri = product.productImageUri,
                                        contentDescription = product.productName,
                                        contentScale = ContentScale.Crop,
                                        loading = { CircularProgressIndicator() },
                                        shapeCrop = CircleShape,
                                        size = 70.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${index + 1}. ${product.productName}",
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier)
                            if (selectedProductProfitable != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShowChart,
                                        contentDescription = stringResource(R.string.MostProfitableProducts),
                                        modifier = Modifier.size(25.dp),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(
                                            id = R.string.ProfitsForProduct,
                                            selectedProductProfitable!!.productName,
                                            run {
                                                val currentDate = Calendar.getInstance()
                                                val xDaysAgo = Calendar.getInstance()
                                                xDaysAgo.add(
                                                    Calendar.DAY_OF_MONTH,
                                                    -(statsInterval - 1)
                                                )
                                                "${
                                                    xDaysAgo.get(Calendar.DAY_OF_MONTH).toString()
                                                        .padStart(2, '0')
                                                }.${
                                                    (xDaysAgo.get(Calendar.MONTH) + 1).toString()
                                                        .padStart(2, '0')
                                                } - ${
                                                    currentDate.get(Calendar.DAY_OF_MONTH)
                                                        .toString().padStart(2, '0')
                                                }.${
                                                    (currentDate.get(Calendar.MONTH) + 1).toString()
                                                        .padStart(2, '0')
                                                }"
                                            }
                                        ),
                                        color = MaterialTheme.colors.onBackground,
                                        fontSize = 16.sp,
                                    )
                                }
                                Chart(
                                    data = topProfitableChartData, modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                Spacer(modifier = Modifier)
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(refreshing = isLoading, state = pullRefreshState, modifier = Modifier.align(
                Alignment.TopCenter))
        }
    }
}