package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R
import vlad.dima.sales.ui.settings.SettingsActivity

@Composable
fun SalesmanPastSalesAppBar(
    isHintVisible: Boolean,
    onHintClick: () -> Unit
) {
    val localContext = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f),
            elevation = dimensionResource(id = R.dimen.standard_elevation),
            color = MaterialTheme.colors.primary
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localContext.getString(R.string.DashboardSales),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(16.dp)
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
        AnimatedVisibility(visible = isHintVisible) {
            Box(
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                        .border(
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colors.surface.copy(.6f)
                            ),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
                        )
                        .clickable { onHintClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(id = R.string.Info)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.PastSalesDescription),
                        color = MaterialTheme.colors.onBackground.copy(.5f),
                    )
                }
            }
        }
        }
    }