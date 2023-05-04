package vlad.dima.sales.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import vlad.dima.sales.R

@Composable
fun FilterButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    defaultColor: Color = MaterialTheme.colors.onSurface,
    activeColor: Color = MaterialTheme.colors.secondaryVariant,
    textColor: Color = MaterialTheme.colors.onSurface,
    backgroundColor: Color = MaterialTheme.colors.surface
) {
    OutlinedButton(
        onClick = onClick,
        elevation = ButtonDefaults.elevation(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            ButtonDefaults.outlinedBorder.width,
            if (!isActive) defaultColor else activeColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 10.dp)
    ) {
        if (!isActive) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = label,
                tint = defaultColor
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = label,
                tint = activeColor
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = textColor)
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