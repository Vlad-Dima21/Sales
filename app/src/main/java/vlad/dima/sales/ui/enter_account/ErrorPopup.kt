package vlad.dima.sales.ui.enter_account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vlad.dima.sales.R
import vlad.dima.sales.ui.theme.DarkSurface
import vlad.dima.sales.ui.theme.LightSurface

@Composable
fun ErrorPopup(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSystemInDarkTheme()) DarkSurface else LightSurface,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
            )
            .border(
                width = dimensionResource(id = R.dimen.border_width),
                color = Color.Red,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
            )
            .fillMaxWidth(0.8f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center),
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}