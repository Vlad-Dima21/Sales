package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order.ProductItem

@Composable
fun SalesmanPastSales(viewModel: SalesmanPastSalesViewModel) {
    val context = LocalContext.current
    var url by remember {
        mutableStateOf(Uri.EMPTY)
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SalesmanPastSalesAppBar()
        ProductItem(imageUri = url, product = Product("", "6A5SPA3L", "Set Burghie Spirale", "Set burghie puternice cu tăiş pe dreapta", 5, 0, 20.99f, 100))
    }
    LaunchedEffect(key1 = true) {
        url = FirebaseStorage.getInstance().reference.child("product_images").child("6A5SPA3L.png").downloadUrl.await()
    }
}