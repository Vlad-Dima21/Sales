package vlad.dima.sales.ui.dashboard.manager_dashboard.products_stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R

@Composable
fun ProductsStatsAppBar(
    onSelectInterval: (interval: Int) -> Unit
) {
    var selectedInterval by rememberSaveable {
        mutableStateOf(7)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        color = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.DashboardProductsStats),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colors.background)
                    .padding(4.dp)
                    .fillMaxWidth(.9f)
            ) {
                Row {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                        .border(
                            1.dp,
                            if (selectedInterval == 7) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onSurface,
                            RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                        )
                        .clickable {
                            onSelectInterval(7)
                            selectedInterval = 7
                        }
                        .weight(1f)
                        .background(
                            when (selectedInterval) {
                                7 -> MaterialTheme.colors.surface
                                else -> MaterialTheme.colors.background
                            }
                        )
                        .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.Last7Days),
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.onSurface.let { if (selectedInterval == 7) it else it.copy(.5f) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                        .border(
                            1.dp,
                            if (selectedInterval == 30) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onSurface,
                            RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                        )
                        .clickable {
                            onSelectInterval(30)
                            selectedInterval = 30
                        }
                        .weight(1f)
                        .background(
                            when (selectedInterval) {
                                30 -> MaterialTheme.colors.surface
                                else -> MaterialTheme.colors.background
                            }
                        )
                        .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.Last30Days),
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.onSurface.let { if (selectedInterval == 30) it else it.copy(.5f) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}