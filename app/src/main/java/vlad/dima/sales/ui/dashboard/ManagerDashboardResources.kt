package vlad.dima.sales.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import vlad.dima.sales.R

sealed class ManagerDashboardResources (
    route: String,
    title: Int,
    icon: ImageVector,
    iconSelected: ImageVector
) : DashboardResource(route, title, icon, iconSelected) {
    object Notifications: ManagerDashboardResources("notifications",
        R.string.DashboardNotifications, Icons.Outlined.Notifications, Icons.Filled.Notifications)

    object ProductsStats: ManagerDashboardResources("productsStats",
        R.string.DashboardProductsStats, Icons.Outlined.Inventory2, Icons.Filled.Inventory2)
}