package vlad.dima.sales.ui.dashboard.manager_dashboard.salesmen_stats

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.rounded.GroupOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.composables.Chart
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ManagerSalesmenStatsPage(viewModel: ManagerSalesmenStatsViewModel) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statsInterval by viewModel.statsInterval.collectAsState()
    val selectedSalesmanBySales by viewModel.selectedSalesmanSelling.collectAsState()
    val selectedSalesmanByProfit by viewModel.selectedSalesmanProfitable.collectAsState()
    val topSalesmenBySales by viewModel.salesmenBySales.collectAsState()
    val salesmenBySalesChartData by viewModel.salesmenBySalesChartData.collectAsState()
    val salesmenByProfitChartData by viewModel.salesmenByProfitChartData.collectAsState()
    val topSalesmenByProfit by viewModel.salesmenByProfit.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = viewModel::loadOrdersAndSalesmen
    )

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        ManagerSalesmenStatsAppBar(onSelectInterval = { interval ->
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
                verticalArrangement = if (topSalesmenBySales.isEmpty() && topSalesmenByProfit.isEmpty()) Arrangement.Center else Arrangement.Top
            ) {
                if (topSalesmenBySales.isEmpty() && topSalesmenByProfit.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(80.dp),
                                imageVector = Icons.Rounded.GroupOff,
                                contentDescription = stringResource(id = R.string.NoSalesmenStatsAvailable),
                                tint = MaterialTheme.colors.onBackground
                            )
                            Text(
                                text = stringResource(id = R.string.NoSalesmenStatsAvailable),
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
                                    contentDescription = stringResource(R.string.SalesmenWithMostSales),
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.SalesmenWithMostSales),
                                    color = MaterialTheme.colors.onBackground,
                                    fontSize = 22.sp,
                                )
                            }
                            Divider(
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(.7f)
                            )
                            topSalesmenBySales.forEachIndexed { index, salesman ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                                        .clickable {
                                            viewModel.setSellingSalesman(salesman)
                                        }
                                        .let {
                                            if (salesman == selectedSalesmanBySales) {
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
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${index + 1}. ${salesman.fullName}",
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier)
                            if (selectedSalesmanBySales != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShowChart,
                                        contentDescription = stringResource(R.string.SalesmenWithMostSales),
                                        modifier = Modifier.size(25.dp),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(
                                            id = R.string.SalesForProduct,
                                            selectedSalesmanBySales!!.fullName,
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
                                    data = salesmenBySalesChartData, modifier = Modifier
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
                                    contentDescription = stringResource(R.string.SalesmenWithMostProfits),
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.SalesmenWithMostProfits),
                                    color = MaterialTheme.colors.onBackground,
                                    fontSize = 22.sp,
                                )
                            }
                            Divider(
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(.7f)
                            )
                            topSalesmenByProfit.forEachIndexed { index, salesman ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                                        .clickable {
                                            viewModel.setProfitableSalesman(salesman)
                                        }
                                        .let {
                                            if (salesman == selectedSalesmanByProfit) {
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
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${index + 1}. ${salesman.fullName}",
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier)
                            if (selectedSalesmanByProfit != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShowChart,
                                        contentDescription = stringResource(R.string.SalesmenWithMostProfits),
                                        modifier = Modifier.size(25.dp),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(
                                            id = R.string.ProfitsForProduct,
                                            selectedSalesmanByProfit!!.fullName,
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
                                    data = salesmenByProfitChartData, modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                Spacer(modifier = Modifier)
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isLoading, state = pullRefreshState, modifier = Modifier.align(
                    Alignment.TopCenter
                )
            )
        }
    }
}