package vlad.dima.sales.ui.dashboard.common

import androidx.compose.animation.*
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import vlad.dima.sales.ui.dashboard.DashboardResource
import vlad.dima.sales.ui.theme.GreenPrimary
import vlad.dima.sales.R

@Composable
fun RowScope.AnimatedBottomNavigationItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    resource: DashboardResource
) {
    val context = LocalContext.current
    val durationMillis = context.resources.getInteger(R.integer.durationMillis)
    val title = context.getString(resource.title)
    BottomNavigationItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (isSelected) {
                    true -> {
                        Icon(
                            imageVector = resource.iconSelected,
                            contentDescription = title
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = resource.icon,
                            contentDescription = title
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(keyframes { this.durationMillis = durationMillis }) + expandVertically(keyframes { this.durationMillis = durationMillis }),
                    exit = fadeOut(keyframes { this.durationMillis = durationMillis }) + shrinkVertically(keyframes { this.durationMillis = durationMillis })
                ) {
                    Text(
                        text = title,
                        color = GreenPrimary
                    )
                }
            }
        },
        selectedContentColor = GreenPrimary,
        unselectedContentColor = Color.Gray
    )
}