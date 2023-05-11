package vlad.dima.sales.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import vlad.dima.sales.R

/**
 * circular progress bar takes up at least 48dp in height, so all buttons must be at least 48dp tall
 */

@Composable
fun FilterButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    defaultColor: Color = MaterialTheme.colors.onSurface,
    activeColor: Color = MaterialTheme.colors.secondaryVariant,
    textColor: Color = MaterialTheme.colors.onSurface,
    backgroundColor: Color = MaterialTheme.colors.surface,
    isEnabled: Boolean = true
) {
    val finalDefaultColor = if (isEnabled) defaultColor else defaultColor.copy(0.5f)
    val finalActiveColor = if (isEnabled) activeColor else activeColor.copy(0.5f)
    val finalTextColor = if (isEnabled) textColor else textColor.copy(0.5f)

    OutlinedButton(
        modifier = Modifier.heightIn(min = 48.dp),
        onClick = onClick,
        elevation = ButtonDefaults.elevation(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            ButtonDefaults.outlinedBorder.width,
            if (!isActive) finalDefaultColor else finalActiveColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 10.dp),
        enabled = isEnabled
    ) {
        if (!isActive) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = label,
                tint = finalDefaultColor
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = label,
                tint = finalActiveColor
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = finalTextColor)
    }
}

@Composable
fun IconButton(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    buttonColor: Color = MaterialTheme.colors.primary,
    contentColor: Color = MaterialTheme.colors.onPrimary
) {
    Button(
        modifier = Modifier.heightIn(min = 48.dp),
        onClick = onClick,
        elevation = ButtonDefaults.elevation(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonColor,
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 10.dp),
        enabled = isEnabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = contentColor)
    }
}

@Composable
fun LoadingButton(
    label: String,
    buttonColor: Color = MaterialTheme.colors.primary,
    contentColor: Color = MaterialTheme.colors.onPrimary
) {
    Button(
        modifier = Modifier.heightIn(min = 48.dp),
        onClick = { },
        elevation = ButtonDefaults.elevation(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonColor,
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 10.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = contentColor)
    }
}