package vlad.dima.sales.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.graphics.vector.ImageVector
import vlad.dima.sales.R

sealed class SalesmanDashboardResource(
    route: String,
    title: Int,
    icon: ImageVector,
    iconSelected: ImageVector
) : DashboardResource(route, title, icon, iconSelected) {
    object Notifications: SalesmanDashboardResource("notifications",R.string.DashboardNotifications, Icons.Outlined.Notifications, Icons.Filled.Notifications)
    object PastSales: SalesmanDashboardResource("past_sales",R.string.DashboardSales, Icons.Outlined.Article, Icons.Filled.Article)
    object Clients: SalesmanDashboardResource("clients",R.string.DashboardClients, Icons.Outlined.People, Icons.Filled.People)
}