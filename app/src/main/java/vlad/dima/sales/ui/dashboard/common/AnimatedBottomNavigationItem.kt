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

@Composable
fun RowScope.AnimatedBottomNavigationItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    resource: DashboardResource
) {
    val context = LocalContext.current
    BottomNavigationItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (isSelected) {
                    true -> {
                        Icon(
                            imageVector = resource.iconSelected,
                            contentDescription = context.getString(resource.title)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = resource.icon,
                            contentDescription = context.getString(resource.title)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(keyframes { durationMillis = 100 }) + expandVertically(keyframes { durationMillis = 100 }),
                    exit = fadeOut(keyframes { durationMillis = 100 }) + shrinkVertically(keyframes { durationMillis = 100 })
                ) {
                    Text(
                        text = context.getString(resource.title),
                        color = GreenPrimary
                    )
                }
            }
        },
        selectedContentColor = GreenPrimary,
        unselectedContentColor = Color.Gray
    )
}