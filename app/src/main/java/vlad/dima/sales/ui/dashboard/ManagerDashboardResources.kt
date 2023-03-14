package vlad.dima.sales.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import vlad.dima.sales.R

sealed class ManagerDashboardResources (
    val route: String,
    val title: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    object Notifications: SalesmanDashboardResources("notifications",
        R.string.DashboardNotifications, Icons.Outlined.Notifications, Icons.Filled.Notifications)
}