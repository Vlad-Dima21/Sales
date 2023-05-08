package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import vlad.dima.sales.R
import vlad.dima.sales.ui.composables.IconLabeledText
import vlad.dima.sales.ui.composables.TextFieldWithValidation
import vlad.dima.sales.ui.dashboard.common.products.Product
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProductItem(
    product: Product,
    modifier: Modifier = Modifier,
    quantityChangedCallback: ((oldValue: Int, newValue: Int, product: Product) -> Unit),
) {
    val context = LocalContext.current
    val cornerRadius = context.resources.getDimension(R.dimen.rounded_corner_radius)
    var selectedQuantity by rememberSaveable {
        mutableStateOf("0")
    }
    var validationMessage by rememberSaveable {
        mutableStateOf("")
    }
    val isValidQuantity by remember(key1 = selectedQuantity) {
        derivedStateOf {
            if (selectedQuantity.isNotEmpty()) {
                val value = selectedQuantity.trim().toInt()
                if (value % product.quantitySold != 0) {
                    validationMessage = context.getString(R.string.ProductIncorrectValue)
                    return@derivedStateOf false
                }
                if (value > product.stock) {
                    validationMessage = context.getString(R.string.ProductUnavailableStock)
                    return@derivedStateOf false
                }
                val oldValue = product.quantityAdded
                product.quantityAdded = value
                // call this lambda only if value is different, otherwise is called after every recomposition
                if (oldValue != value) {
                    quantityChangedCallback(oldValue, value, product)
                }
                validationMessage = ""
                return@derivedStateOf true
            }
            validationMessage = context.getString(R.string.FieldRequired)
            return@derivedStateOf false
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(
        modifier = modifier,
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
                    .height(IntrinsicSize.Max),
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.productImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.productName,
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(80.dp)
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = product.productName,
                        color = MaterialTheme.colors.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
                            IconLabeledText(
                                icon = Icons.Outlined.Payment,
                                label = stringResource(R.string.Price),
                                text = stringResource(id = R.string.PriceFormat, ((product.price * 100.0f).roundToInt() / 100.0f).toString())
                            )
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 8.dp)
                                .animateContentSize(),
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
@Preview
fun ProductItemPreview() {
    Box(
        Modifier.fillMaxSize()
    ) {
        ProductItem(
            product = Product(
                "",
                "213",
                "Amongus",
                Uri.parse("https://s13emagst.akamaized.net/products/28743/28742947/images/res_13ff985e86dd5eb605b19edd0435c15a.jpg"),
                "blablablablablablablablablablablablablablablablablablablabla",
                10,
                0,
                20f,
                stock = 100
            ),
            quantityChangedCallback = { _, _, _ -> }
        )
    }
}

@Composable
fun image(
    url: String
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp)
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "blabla",
                color = MaterialTheme.colors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}