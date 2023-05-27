package vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R

@Composable
fun SalesmanNotificationsAppBar(
    viewModel: SalesmanNotificationsViewModel
) {
    val localContext = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        color = MaterialTheme.colors.primary
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.LatestNotifications),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            )
            ClickableText(
                text = AnnotatedString(
                    text = stringResource(id = R.string.LogOut)
                ),
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}