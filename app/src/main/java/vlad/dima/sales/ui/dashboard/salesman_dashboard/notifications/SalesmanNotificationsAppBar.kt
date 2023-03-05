package vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R

@Composable
fun SalesmanNotificationsAppBar() {
    val localContext = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 10.dp,
        color = MaterialTheme.colors.primary
    ) {
        Text(
            text = localContext.getString(R.string.DashboardNotifications),
            fontSize = 20.sp,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}