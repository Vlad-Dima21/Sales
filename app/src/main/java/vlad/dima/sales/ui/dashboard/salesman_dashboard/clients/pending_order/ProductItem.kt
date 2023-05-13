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
import vlad.dima.sales.ui.composables.IconLabeledFlexText
import vlad.dima.sales.ui.composables.IconLabeledText
import vlad.dima.sales.ui.composables.TextFieldWithValidation
import vlad.dima.sales.ui.dashboard.common.products.Product
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProductItem(
    productItemHolder: PendingOrderViewModel.ProductItemHolder,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cornerRadius = context.resources.getDimension(R.dimen.rounded_corner_radius)
    val selectedQuantity = productItemHolder.quantityString
    val validationMessage = productItemHolder.validationMessage
    val product = productItemHolder.product
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
        border = when (validationMessage == null) {
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
                    IconLabeledFlexText(
                        icon = Icons.Outlined.Notes,
                        label = context.getString(R.string.ProductDescription),
                        text = product.productDescription
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
                                    productItemHolder.changeQuantity(newValue)
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
                                isError = validationMessage != null,
                                errorMessage = if (validationMessage != null) stringResource(id = validationMessage) else ""
                            )
                        }
                    }

                }
            }
        }
    }
}