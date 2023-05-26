package vlad.dima.sales.ui.dashboard.manager_dashboard.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.model.Notification
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.dashboard.common.notifications.NotificationCard
import java.lang.Integer.max
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ManagerNotificationsPage(viewModel: ManagerNotificationsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val refreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing, { viewModel.loadItems() })
    val lazyListState = rememberLazyListState()
    val fabIsVisible by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ManagerNotificationsAppBar(viewModel)
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = lazyListState
            ) {
                item {
                    // without the spacer first, the elevation shadow of the first card is cut off
                    Spacer(modifier = Modifier)
                }
                items(
                    items = viewModel.items,
                    key = { it.createdDate }
                ) { notification ->
                    val previousIndex = viewModel.items.indexOf(notification) - 1
                    val previousNotification = viewModel.items[max(previousIndex, 0)]

                    if (previousIndex == -1 || DateFormat.getDateInstance()
                            .format(notification.createdDate) != DateFormat.getDateInstance()
                            .format(previousNotification.createdDate)
                    ) {
                        Box(
                            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
                        ) {
                            val isCurrentDate = DateFormat.getDateInstance()
                                .format(notification.createdDate) == DateFormat.getDateInstance()
                                .format(Date())
                            Text(
                                text = if (isCurrentDate) stringResource(id = R.string.Today) else DateFormat.getDateInstance()
                                    .format(notification.createdDate),
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

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                visible = fabIsVisible && networkStatus == NetworkManager.NetworkStatus.Available,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.createNewNotification()
                        }
                    },
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.AddNotification),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}