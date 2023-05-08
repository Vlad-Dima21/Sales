package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
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
import kotlinx.coroutines.flow.StateFlow
import vlad.dima.sales.R
import vlad.dima.sales.ui.composables.FilterButton
import vlad.dima.sales.ui.composables.LabeledText
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import vlad.dima.sales.ui.theme.extra
import java.util.logging.Filter

@Composable
fun PendingOrderAppBar(
    viewModel: PendingOrderViewModel,
    client: Client,
    isCollapsed: Boolean = false
) {
    val localContext = LocalContext.current
    val totalPrice by viewModel.totalPrice.collectAsState("0")
    val onlyInStock by viewModel.onlyInStock.collectAsState()
    val onlyInCart by viewModel.onlyInCart.collectAsState()
    val onBackgroundTint = MaterialTheme.colors.onBackground
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
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
                LabeledText(
                    label = stringResource(R.string.TotalPrice),
                    text = stringResource(id = R.string.PriceFormat, totalPrice),
                    textColor = MaterialTheme.colors.extra,
                    textSize = 18.sp
                )
                Row(
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
                    vlad.dima.sales.ui.composables.IconButton(
                        label = stringResource(id = R.string.Save),
                        isEnabled = totalPrice != "0",
                        onClick = { /*TODO*/ },
                        icon = Icons.Filled.ShoppingCart,
                        buttonColor = MaterialTheme.colors.extra
                    )
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