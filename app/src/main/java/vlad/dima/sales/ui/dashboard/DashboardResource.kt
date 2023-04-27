package vlad.dima.sales.ui.dashboard

import androidx.compose.ui.graphics.vector.ImageVector

sealed class DashboardResource (
    val route: String,
    val title: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector
)