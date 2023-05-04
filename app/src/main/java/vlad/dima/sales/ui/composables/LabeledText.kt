package vlad.dima.sales.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.ui.theme.italicText

@Composable
fun LabeledText(
    modifier: Modifier = Modifier,
    label: String,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    textSize: TextUnit = 14.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, color = labelColor, fontSize = textSize)
        Divider(
            modifier = Modifier
                .width(2.dp)
                .height(2.dp),
            color = MaterialTheme.colors.onSurface
        )
        Text(text = text, color = textColor, style = MaterialTheme.typography.italicText, fontSize = textSize)
    }
}

@Composable
fun IconLabeledText(
    modifier: Modifier = Modifier,
    icon: Painter,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(text = label, color = labelColor)
        Divider(
            modifier = Modifier
                .width(2.dp)
                .height(2.dp),
            color = MaterialTheme.colors.onSurface
        )
        Text(text = text, color = textColor, style = MaterialTheme.typography.italicText)
    }
}

@Composable
fun IconLabeledText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    oneLine: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(text = label, color = labelColor)
        Divider(
            modifier = Modifier
                .width(2.dp)
                .height(2.dp),
            color = MaterialTheme.colors.onSurface
        )
        if (!oneLine) {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText)
        } else {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText,  maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}