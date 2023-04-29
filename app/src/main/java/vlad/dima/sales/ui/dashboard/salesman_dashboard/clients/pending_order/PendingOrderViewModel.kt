package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client

class PendingOrderViewModel(
    private val client: Client,
    private val repository: OrderRepository
): ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val productsCollection = Firebase.firestore.collection("products")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var products by mutableStateOf(listOf<Product>())


    private fun loadAvailableProducts() = CoroutineScope(Dispatchers.IO).launch {
        _isLoading.emit(true)

        products = productsCollection.whereGreaterThan("stock", 0).get().await().documents.map {snapshot ->
            val product = snapshot.toObject(Product::class.java)!!
            product.productId = snapshot.id
            product
        }

        _isLoading.emit(false)
    }

    class Factory(private val client: Client, private val repository: OrderRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PendingOrderViewModel::class.java)) {
                return PendingOrderViewModel(client, repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}