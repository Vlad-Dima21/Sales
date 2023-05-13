package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import vlad.dima.sales.ui.composables.IconLabeledFlexText
import vlad.dima.sales.ui.composables.IconLabeledText
import vlad.dima.sales.ui.dashboard.common.notifications.Notification
import vlad.dima.sales.ui.theme.Orange
import vlad.dima.sales.ui.theme.italicText
import java.util.*

@Composable
fun NotificationChatAppBar(
    notification: Notification,
    isManager: Boolean,
    onDelete: () -> Unit
) {
    val importanceColor = when (notification.importance) {
    1 -> Orange
    2 -> Color.Red
    else -> MaterialTheme.colors.onSurface
}
    val importanceText = when (notification.importance) {
        1 -> stringResource(id = R.string.HightImportance)
        2 -> stringResource(id = R.string.AlertImportance)
        else -> stringResource(id = R.string.NormalImportance)
    }
    val localContext = LocalContext.current
    val time = remember(notification) {
        val calendar = Calendar.getInstance()
        calendar.time = notification.createdDate
        "${
            calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        }:${calendar.get(Calendar.MINUTE).toString().padStart(2, '0')}"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        contentColor = MaterialTheme.colors.surface,
        elevation = dimensionResource(id = R.dimen.standard_elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { (localContext as Activity).finish() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.GoBack),
                        tint = MaterialTheme.colors.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = notification.title,
                    fontSize = 22.sp,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                if (isManager) {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.DeleteNotification),
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
            Divider(modifier = Modifier
                .fillMaxWidth()
                .height(2.dp))
            IconLabeledText(
                icon = Icons.Outlined.PrivacyTip,
                label = stringResource(id = R.string.NotificationImportanceLabel),
                labelSize = 16.sp,
                text = importanceText,
                textSize = 16.sp,
                textColor = importanceColor
            )
            IconLabeledText(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.NotificationTime),
                text = time,
                textSize = 15.sp,
                oneLine = true
            )
            IconLabeledFlexText(
                icon = Icons.Outlined.Notes,
                label = stringResource(R.string.ProductDescription),
                labelSize = 16.sp,
                text = notification.description,
                textSize = 16.sp
            )
        }
    }
}