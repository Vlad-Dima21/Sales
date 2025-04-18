package vlad.dima.sales.ui.dashboard.salesman_dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.Notification
import vlad.dima.sales.ui.dashboard.common.notifications.NotificationCard
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsAppBar
import java.lang.Integer.max
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SalesmanNotificationsPage(viewModel: SalesmanNotificationsViewModel) {
    val refreshing by viewModel.isRefreshing.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing, { viewModel.loadItems() })

    Column(modifier = Modifier.fillMaxSize()) {
        SalesmanNotificationsAppBar(viewModel)
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .let {
                    if (networkStatus == NetworkManager.NetworkStatus.Available) {
                        return@let it.pullRefresh(pullRefreshState)
                    }
                    it
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // without the spacer first, the elevation shadow of the first card is cut off
                    Spacer(modifier = Modifier)
                }
                items(
                    items = notifications,
                    key = { it.createdDate }
                ) { notification ->
                    val previousNotification: Notification
                    val previousIndex = notifications.indexOf(notification) - 1
                    previousNotification = notifications[max(previousIndex, 0)]

                    if (previousIndex == -1 || DateFormat.getDateInstance().format(notification.createdDate) != DateFormat.getDateInstance().format(previousNotification.createdDate)) {
                        Box(
                            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
                        ) {
                            val isCurrentDate = DateFormat.getDateInstance().format(notification.createdDate) == DateFormat.getDateInstance().format(Date())
                            Text(
                                text = if (isCurrentDate) stringResource(id = R.string.Today) else DateFormat.getDateInstance().format(notification.createdDate),
                                modifier = Modifier
                                    .align(Alignment.CenterStart),
                                color = MaterialTheme.colors.onBackground,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    }
                    NotificationCard(
                        notification = notification,
                        modifier = Modifier.animateItemPlacement(),
                        viewModel = viewModel
                    )
                }
                item {
                    Spacer(modifier = Modifier)
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing, state = pullRefreshState, Modifier.align(
                    Alignment.TopCenter
                )
            )

            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Feedback,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.NoNotifications), color = MaterialTheme.colors.onBackground)
                }
            }
        }
    }
}