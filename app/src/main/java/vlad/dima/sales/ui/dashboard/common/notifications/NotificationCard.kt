package vlad.dima.sales.ui.dashboard.common.notifications

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.dashboard.common.notifications.notification_chat.NotificationChatActivity
import vlad.dima.sales.ui.theme.Orange

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationCard(
    id: String,
    title: String,
    description: String,
    importance: Int,
    viewModel: NotificationsViewModel,
    modifier: Modifier = Modifier
) {
    val shortenedDescription = if (description.length <= 100) description else "${description.slice(0..99)}..."
    val importanceColor = when (importance) {
        1 -> Orange
        2 -> Color.Red
        else -> MaterialTheme.colors.onSurface
    }
    val localContext = LocalContext.current
    Card(
        modifier = modifier,
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        onClick = {
                viewModel.viewNotification(
                    Intent(localContext, NotificationChatActivity::class.java)
                        .putExtra("id", id)
                        .putExtra("title", title)
                        .putExtra("description", description)
                        .putExtra("importance", importance)
                )
        },
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .fillMaxSize(),

            ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = importanceColor,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(text = shortenedDescription, fontSize = 16.sp)
        }
    }
}