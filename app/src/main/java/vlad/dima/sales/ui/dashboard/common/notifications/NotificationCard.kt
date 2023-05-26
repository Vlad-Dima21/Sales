package vlad.dima.sales.ui.dashboard.common.notifications

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.model.Notification
import vlad.dima.sales.ui.composables.IconLabeledText
import vlad.dima.sales.ui.dashboard.common.notifications.notification_chat.NotificationChatActivity
import vlad.dima.sales.ui.theme.Orange
import vlad.dima.sales.ui.theme.italicText
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationCard(
    notification: Notification,
    viewModel: NotificationsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colors.onSurface
    val importanceColor = remember {
        when (notification.importance) {
            1 -> Orange
            2 -> Color.Red
            else -> surfaceColor
        }
    }
    val importanceText = remember {
        when (notification.importance) {
            1 -> context.getString(R.string.HightImportance)
            2 -> context.getString(R.string.AlertImportance)
            else -> context.getString(R.string.NormalImportance)
        }
    }
    val localContext = LocalContext.current
    val time = remember {
        val calendar = Calendar.getInstance()
        calendar.time = notification.createdDate
        "${
            calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        }:${calendar.get(Calendar.MINUTE).toString().padStart(2, '0')}"
    }
    Card(
        modifier = modifier,
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        onClick = {
            viewModel.viewNotification(
                Intent(localContext, NotificationChatActivity::class.java)
                    .putExtra("id", notification.id)
                    .putExtra("title", notification.title)
                    .putExtra("description", notification.description)
                    .putExtra("importance", notification.importance)
            )
        },
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 20.sp
                )
                IconLabeledText(
                    icon = Icons.Outlined.Notes,
                    label = stringResource(R.string.ProductDescription),
                    text = notification.description,
                    textSize = 15.sp,
                    oneLine = true
                )
                IconLabeledText(
                    icon = Icons.Outlined.Schedule,
                    label = stringResource(R.string.NotificationTime),
                    text = time,
                    textSize = 15.sp,
                    oneLine = true
                )
            }
            Row(
                modifier = Modifier
                    .widthIn(min = 75.dp)
                    .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Outlined.PrivacyTip,
                    contentDescription = stringResource(id = R.string.NotificationImportanceLabel),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = importanceText,
                    color = importanceColor,
                    style = MaterialTheme.typography.italicText
                )
            }
        }
    }
}