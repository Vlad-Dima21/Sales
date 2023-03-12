package vlad.dima.sales.ui.dashboard.salesman_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsAppBar

@Composable
fun SalesmanNotificationsPage(viewModel: SalesmanNotificationsViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        SalesmanNotificationsAppBar(viewModel)
        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // without the spacer first, the elevation shadow of the first card is cut off
                Spacer(modifier = Modifier)
            }
            if (viewModel.itemsLoaded) {
                items(
                    items = viewModel.items
                ) {notification ->
                    NotificationCard(
                        title = notification.title,
                        description = notification.description,
                        importance = notification.importance
                    )
                }
            } else {
                item { CircularProgressIndicator() }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationCard(
    title: String,
    description: String,
    importance: Int,
    modifier: Modifier = Modifier
) {
    val shortenedDescription = if (description.length <= 100) description else "${description.slice(0..99)}..."
    Card(
        modifier = modifier,
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        onClick = {},
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .fillMaxSize(),

        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 36.sp, modifier = Modifier.padding(bottom = 5.dp))
            Text(text = shortenedDescription, fontSize = 16.sp)
        }
    }
}