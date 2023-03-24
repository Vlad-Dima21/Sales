package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.theme.Orange

@Composable
fun NotificationChatAppBar(
    title: String,
    description: String,
    importance: Int,
    isManager: Boolean,
    onDelete: () -> Unit
) {
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
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = when (importance) {
                        1 -> Orange
                        2 -> Color.Red
                        else -> MaterialTheme.colors.onSurface
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                )
                if (isManager) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.DeleteNotification),
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp)
            Text(
                text = description,
                fontSize = 20.sp,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}