package vlad.dima.sales.ui.dashboard.common.notifications

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.theme.Orange

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationCard(
    title: String,
    description: String,
    importance: Int,
    modifier: Modifier = Modifier
) {
    val shortenedDescription = if (description.length <= 100) description else "${description.slice(0..99)}..."
    val importanceColor = when (importance) {
        1 -> Orange
        2 -> Color.Red
        else -> MaterialTheme.colors.onSurface
    }
    Card(
        modifier = modifier,
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        onClick = {},
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