package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import vlad.dima.sales.R
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.ui.composables.LabeledText
import vlad.dima.sales.ui.theme.italicText
import java.text.DateFormat
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SaleClient(
    modifier: Modifier,
    saleClient: SaleClient,
    isPastSale: Boolean = false,
    onOrderClick: ((clientId: String, orderId: Int) -> Unit)? = null,
    onOrderOptionClick: ((option: OrderContextOption, order: Order) -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            dimensionResource(id = R.dimen.border_width),
            MaterialTheme.colors.surface.copy(.6f)
        ),
        backgroundColor = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Business,
                    contentDescription = saleClient.client.clientName
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = saleClient.client.clientName,
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = saleClient.orderList,
                    key = { it.order.orderId }
                ) {
                    SaleOrder(
                        modifier = Modifier.animateItemPlacement(),
                        saleOrder = it,
                        isPastSale = isPastSale,
                        onOrderClick = onOrderClick,
                        onOrderOptionClick = onOrderOptionClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaleOrder(
    modifier: Modifier = Modifier,
    saleOrder: SaleOrder,
    isPastSale: Boolean = false,
    onOrderClick: ((clientId: String, orderId: Int) -> Unit)? = null,
    onOrderOptionClick: ((option: OrderContextOption, order: Order) -> Unit)? = null
) {
    var isDropDownVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val createdDate = remember {
        DateFormat.getDateInstance().format(saleOrder.order.createdDate)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        color = MaterialTheme.colors.surface.copy(.5f),
        border = if (!isPastSale && (saleOrder.hasMissingProducts || saleOrder.hasInsufficientStock)) BorderStroke(1.dp, MaterialTheme.colors.error) else null
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .let {
                    if (onOrderClick != null) {
                        it.combinedClickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = {
                                with(saleOrder) {
                                    onOrderClick(order.clientId, order.orderId)
                                }
                            },
                            onLongClick = {
                                isDropDownVisible = true
                            }
                        )
                    } else {
                        it
                    }
                }

        ) {
            Column(
                Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.OrderNumber, saleOrder.order.orderId),
                        color = MaterialTheme.colors.onSurface
                    )
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        LabeledText(
                            label = stringResource(id = R.string.TotalPrice),
                            text = stringResource(
                                id = R.string.PriceFormat,
                                totalToString(saleOrder.order.total)
                            )
                        )
                        if (isPastSale) {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = createdDate,
                                color = MaterialTheme.colors.onSurface.copy(.8f),
                                style = MaterialTheme.typography.italicText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                LazyRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier)
                    }
                    items(
                        items = saleOrder.products,
                        key = { it.product.productCode }
                    ) {
                        SaleProduct(saleProduct = it)
                    }
                    item {
                        Spacer(modifier = Modifier)
                    }
                }
                if (saleOrder.hasMissingProducts || saleOrder.hasInsufficientStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (saleOrder.hasMissingProducts) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                Modifier
                                    .width(2.dp)
                                    .height(2.dp),
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.MissingProducts),
                                color = MaterialTheme.colors.onSurface.copy(.8f),
                                style = MaterialTheme.typography.italicText
                            )
                        }
                    }
                    if (saleOrder.hasInsufficientStock) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                Modifier
                                    .width(2.dp)
                                    .height(2.dp),
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.OdrderInsufficientStock),
                                color = MaterialTheme.colors.onSurface.copy(.8f),
                                style = MaterialTheme.typography.italicText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (onOrderOptionClick != null) {
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    DropdownMenu(
                        expanded = isDropDownVisible,
                        onDismissRequest = { isDropDownVisible = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onOrderOptionClick(OrderContextOption.Delete, saleOrder.order)
                                isDropDownVisible = false
                            }
                        ) {
                            Text(stringResource(id = R.string.Delete))
                        }
                    }
                }
            }
        }
    }
}

enum class OrderContextOption {
    Delete
}

@Composable
private fun SaleProduct(
    saleProduct: SaleProduct
) {
    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = saleProduct.product.productName, color = MaterialTheme.colors.onBackground)
            LabeledText(
                label = stringResource(id = R.string.Quantity),
                text = saleProduct.amountSold.toString(),
                labelColor = MaterialTheme.colors.secondaryVariant.copy(.8f)
            )
        }
    }
}

private fun totalToString(value: Float): String =
    ((value * 100.0f).roundToInt() / 100.0f).toString()