package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import vlad.dima.sales.R
import vlad.dima.sales.ui.composables.FilterButton
import vlad.dima.sales.ui.composables.LabeledText
import vlad.dima.sales.ui.composables.LoadingButton
import vlad.dima.sales.ui.theme.extra

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PendingOrderAppBar(
    viewModel: PendingOrderViewModel,
    isCollapsed: Boolean = false
) {
    val localContext = LocalContext.current
    val client by viewModel.client.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState("0")
    val onlyInStock by viewModel.onlyInStock.collectAsState()
    val onlyInCart by viewModel.onlyInCart.collectAsState()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val onBackgroundTint = MaterialTheme.colors.onBackground
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f),
        color = MaterialTheme.colors.background,
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        contentColor = onBackgroundTint
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AnimatedVisibility(visible = !isCollapsed) {
                Row {
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp),
                        onClick = { (localContext as Activity).finish() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.GoBack),
                            tint = onBackgroundTint
                        )
                    }
                    Text(
                        text = localContext.getString(R.string.NewOrderFor, client.clientName),
                        fontSize = 24.sp,
                        color = onBackgroundTint,
                        modifier = Modifier
                            .padding(8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledText(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.TotalPrice),
                        text = stringResource(id = R.string.PriceFormat, totalPrice),
                        textColor = MaterialTheme.colors.extra,
                        textSize = 18.sp
                    )
                    AnimatedVisibility(visible = orderStatus == PendingOrderViewModel.OrderStatus.INVALID) {
                        Text(
                            color = Color.Red,
                            text = stringResource(id = R.string.CheckFields)
                        )
                    }
                    AnimatedVisibility(visible = orderStatus == PendingOrderViewModel.OrderStatus.FAILED) {
                        Text(
                            color = Color.Red,
                            text = stringResource(id = R.string.SystemError)
                        )
                    }

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterButton(
                        label = stringResource(id = R.string.OnlyStock),
                        isActive = onlyInStock || onlyInCart,
                        onClick = {
                            viewModel.filterStock()
                        },
                        activeColor = MaterialTheme.colors.extra,
                        backgroundColor = MaterialTheme.colors.background,
                        isEnabled = !onlyInCart
                    )
                    FilterButton(
                        label = stringResource(id = R.string.OnlyCart),
                        isActive = onlyInCart,
                        onClick = {
                            viewModel.filterCart()
                        },
                        activeColor = MaterialTheme.colors.extra,
                        backgroundColor = MaterialTheme.colors.background,
                        isEnabled = totalPrice != "0"
                    )
                    if (orderStatus != PendingOrderViewModel.OrderStatus.LOADING) {
                        AnimatedContent(
                            targetState = totalPrice != "0",
                            transitionSpec = { fadeIn() with fadeOut() }) {
                            vlad.dima.sales.ui.composables.IconButton(
                                label = stringResource(id = R.string.Save),
                                isEnabled = totalPrice != "0",
                                onClick = { viewModel.saveOrder() },
                                icon = Icons.Filled.ShoppingCart,
                                buttonColor = MaterialTheme.colors.extra
                            )
                        }
                    } else {
                        LoadingButton(
                            label = stringResource(id = R.string.Save),
                            buttonColor = MaterialTheme.colors.extra
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun FilterButtonsPreview() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterButton(label = "Products in stock", isActive = true, onClick = {})
        Spacer(Modifier.height(10.dp))
        FilterButton(label = "Products in cart", isActive = false, onClick = {})
    }
}