package vlad.dima.sales.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldWithValidation(
    modifier: Modifier = Modifier,
    value: String,
    maxLength: Int,
    onValueChange: (newValue: String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    isError: Boolean,
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = true,
            textStyle = textStyle
        )
        Divider(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    if (isError) Color.Red else MaterialTheme.colors.primary
                )
        )
        Text(
            modifier = Modifier.alpha(if (isError) 1f else 0f),
            text = errorMessage.ifEmpty { (0 .. maxLength).joinToString() { " " } },
            color = Color.Red,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}