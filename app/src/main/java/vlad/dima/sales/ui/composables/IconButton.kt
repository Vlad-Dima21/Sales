package vlad.dima.sales.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
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
    val finalDefaultColor = remember(isEnabled) { if (isEnabled) defaultColor else defaultColor.copy(0.5f) }
    val finalActiveColor = remember(isEnabled) { if (isEnabled) activeColor else activeColor.copy(0.5f) }
    val finalTextColor = remember(isEnabled) { if (isEnabled) textColor else textColor.copy(0.5f) }

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
            disabledContentColor = finalTextColor
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

enum class SortState {
    None,
    Ascending,
    Descending;

    fun nextState(): SortState {
        return when(this) {
            None -> Ascending
            Ascending -> Descending
            else -> None
        }
    }
}

@Composable
fun SortButton(
    modifier: Modifier = Modifier,
    label: String,
    state: SortState,
    onClick: () -> Unit,
    defaultColor: Color = MaterialTheme.colors.onSurface,
    activeColor: Color = MaterialTheme.colors.secondaryVariant,
    textColor: Color = MaterialTheme.colors.onSurface,
    backgroundColor: Color = MaterialTheme.colors.surface,
    isEnabled: Boolean = true
) {
    val finalDefaultColor = remember(isEnabled) { if (isEnabled) defaultColor else defaultColor.copy(0.5f) }
    val finalActiveColor = remember(isEnabled) { if (isEnabled) activeColor else activeColor.copy(0.5f) }
    val finalTextColor = remember(isEnabled) { if (isEnabled) textColor else textColor.copy(0.5f) }

    OutlinedButton(
        modifier = modifier.heightIn(min = 48.dp),
        onClick = onClick,
        elevation = ButtonDefaults.elevation(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            ButtonDefaults.outlinedBorder.width,
            when (state) {
                SortState.Ascending, SortState.Descending -> finalActiveColor
                else -> finalDefaultColor
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor,
            disabledContentColor = finalTextColor
        ),
        contentPadding = PaddingValues(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 10.dp),
        enabled = isEnabled
    ) {
        Icon(
            imageVector = when (state) {
                SortState.Ascending -> Icons.Outlined.ArrowUpward
                SortState.Descending -> Icons.Outlined.ArrowDownward
                else -> Icons.Outlined.Add
           },
            contentDescription = label,
            tint = when (state) {
                SortState.Ascending, SortState.Descending -> finalActiveColor
                else -> finalDefaultColor
            }
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = finalTextColor, maxLines = 1)

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