package vlad.dima.sales.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    textSize: TextUnit = 14.sp,
    oneLine: Boolean = false
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
        if (!oneLine) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.italicText,
                fontSize = textSize
            )
        } else {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.italicText,
                fontSize = textSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
    textColor: Color = MaterialTheme.colors.onSurface,
    oneLine: Boolean = false
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
        if (!oneLine) {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText)
        } else {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun IconLabeledText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelSize: TextUnit = 14.sp,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    textSize: TextUnit = 14.sp,
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
        Text(text = label, color = labelColor, fontSize = labelSize)
        Divider(
            modifier = Modifier
                .width(2.dp)
                .height(2.dp),
            color = MaterialTheme.colors.onSurface
        )
        if (!oneLine) {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText, fontSize = textSize)
        } else {
            Text(text = text, color = textColor, style = MaterialTheme.typography.italicText,  maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = textSize)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconLabeledFlexText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelSize: TextUnit = 14.sp,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    textSize: TextUnit = 14.sp
) {
    val textWords by remember(text) {
        mutableStateOf(
            text.split(" ")
        )
    }
    FlowRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = labelSize
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier
                .width(2.dp)
                .height(2.dp),
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        textWords.forEach {  word ->
            Text(text = word.plus(" "), color = textColor, style = MaterialTheme.typography.italicText, fontSize = textSize)
        }
    }
}


