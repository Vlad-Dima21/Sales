package vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun SalesmanClientsPage(viewModel: SalesmanClientsViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        SalesmanClientsAppBar()
    }
}