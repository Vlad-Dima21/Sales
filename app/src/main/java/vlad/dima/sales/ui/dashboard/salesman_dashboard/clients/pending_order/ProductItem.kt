package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.composables.TextFieldWithValidation
import vlad.dima.sales.ui.dashboard.common.products.Product

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProductItem(
    product: Product,
    imageUrl: String
) {
    val context = LocalContext.current
    val cornerRadius = context.resources.getDimension(R.dimen.rounded_corner_radius)
    var selectedQuantity by rememberSaveable {
        mutableStateOf("0")
    }
    var validationMessage by rememberSaveable {
        mutableStateOf("")
    }
    val isValidQuantity by remember {
        derivedStateOf {
            if (selectedQuantity.isNotEmpty()) {
                val value = selectedQuantity.trim().toInt()
                product.quantityAdded = value
                if (value % product.quantitySold != 0) {
                    validationMessage = context.getString(R.string.ProductIncorrectValue)
                    return@derivedStateOf false
                }
                if (value > product.stock) {
                    validationMessage = context.getString(R.string.ProductUnavailableStock)
                    return@derivedStateOf false
                }
                validationMessage = ""
                return@derivedStateOf true
            }
            validationMessage = context.getString(R.string.FieldRequired)
            product.quantityAdded = 0
            return@derivedStateOf false
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(
        shape = RoundedCornerShape(cornerRadius),
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
        border = when (isValidQuantity) {
            false -> BorderStroke(1.dp, Color.Red)
            true -> null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = product.productName,
                    color = MaterialTheme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconLabeledText(
                        icon = Icons.Outlined.Notes,
                        label = context.getString(R.string.ProductDescription),
                        text = product.productDescription,
                        oneLine = true
                    )
                    Row {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconLabeledText(
                                icon = Icons.Outlined.LibraryBooks,
                                label = context.getString(R.string.ProductCode),
                                text = product.productCode
                            )
                            IconLabeledText(
                                icon = painterResource(id = R.drawable.product_quantity),
                                label = context.getString(R.string.ProductQuantitySold),
                                text = product.quantitySold.toString()
                            )
                            IconLabeledText(
                                icon = Icons.Outlined.Inventory2,
                                label = context.getString(R.string.ProductStock),
                                text = product.stock.toString()
                            )
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = stringResource(
                                    id = R.string.CartQuantity
                                )
                            )
                            TextFieldWithValidation(
                                value = selectedQuantity,
                                maxLength = 4,
                                onValueChange = { newValue ->
                                    if (newValue.length > 4) {
                                        return@TextFieldWithValidation
                                    }
                                    if (newValue.length > 1 && newValue[0] == '0') {
                                        return@TextFieldWithValidation
                                    }
                                    if (newValue.isNotEmpty()) {
                                        val digits = (0..9).map { it.toString()[0] }
                                        newValue.forEach {
                                            if (!digits.contains(it)) {
                                                return@TextFieldWithValidation
                                            }
                                        }
                                    }
                                    selectedQuantity = newValue
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface
                                ),
                                isError = !isValidQuantity,
                                errorMessage = validationMessage
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun IconLabeledText(
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    oneLine: Boolean = false
) {
    Row(
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
            Text(text = text, color = textColor, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Default)
        } else {
            Text(text = text, color = textColor, fontStyle = FontStyle.Italic, fontFamily = FontFamily.Default, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun IconLabeledText(
    icon: Painter,
    iconColor: Color = MaterialTheme.colors.primaryVariant,
    label: String,
    labelColor: Color = MaterialTheme.colors.onSurface,
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
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
        Text(text = text, color = textColor, fontStyle = FontStyle.Italic)
    }
}

@Composable
@Preview
fun ProductItemPreview() {
    Box(
        Modifier.fillMaxSize()
    ) {
        ProductItem(product = Product("", "213", "Amongus", "blablablablablablablablablablablablablablablablablablablabla", 10, 0, 20f, stock = 100), imageUrl = "")
    }
}