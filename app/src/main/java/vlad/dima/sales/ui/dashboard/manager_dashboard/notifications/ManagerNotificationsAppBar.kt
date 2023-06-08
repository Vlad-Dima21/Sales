package vlad.dima.sales.ui.dashboard.manager_dashboard.notifications

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R
import vlad.dima.sales.ui.settings.SettingsActivity

@Composable
fun ManagerNotificationsAppBar(viewModel: ManagerNotificationsViewModel) {
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
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    localContext.startActivity(
                        Intent(localContext, SettingsActivity::class.java)
                    )
                }
            ) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = stringResource(id = R.string.Options))
            }
        }
    }
}