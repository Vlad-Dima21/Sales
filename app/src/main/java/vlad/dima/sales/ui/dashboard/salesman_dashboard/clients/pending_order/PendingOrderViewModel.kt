package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import kotlin.math.roundToInt

class PendingOrderViewModel(
    private val client: Client,
    private val repository: OrderRepository
): ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val productsCollection = Firebase.firestore.collection("products")
    private val storageReference = FirebaseStorage.getInstance().reference.child("product_images")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var _products = MutableStateFlow(listOf<Product>())

    private var _totalPrice = MutableStateFlow(0f)
    var totalPrice: Flow<String> = _totalPrice.map {
        roundFloat(it).run {
            if (this > 0f) {
                this.toString()
            } else {
                "0"
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "0")

    // product filters
    private val _onlyInStock = MutableStateFlow(false)
    val onlyInStock = _onlyInStock.asStateFlow()

    private val _onlyInCart = MutableStateFlow(false)
    val onlyInCart = _onlyInCart.asStateFlow()

    val products = combine(_products, _onlyInStock, _onlyInCart, _totalPrice) { products, onlyInStock, onlyInCart, _ ->
        var requestedProducts = products
        if (onlyInStock) {
            requestedProducts =  requestedProducts.filter { it.stock > 0 }
        }
        if (onlyInCart) {
            requestedProducts = requestedProducts.filter { it.quantityAdded > 0 }
        }
        return@combine requestedProducts
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    init {
        loadProducts()
    }


    private fun loadProducts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)

        val loadedProducts = productsCollection.orderBy("productName").get().await().documents.map {snapshot ->
            val product = snapshot.toObject(Product::class.java)!!
            product.productId = snapshot.id
            product
        }
            .toMutableList()

        val productImagesReferences = storageReference.listAll().await().items.associate {
            val codeWithoutExtension = it.name.substring(0, it.name.lastIndexOf('.'))
            codeWithoutExtension to it.downloadUrl.await()
        }

        loadedProducts.forEach {product ->
            product.productImageUri = productImagesReferences[product.productCode] ?: Uri.EMPTY
        }

        _products.value = loadedProducts

        _isLoading.emit(false)
    }

    fun filterStock() {
        _onlyInStock.value = !_onlyInStock.value
    }

    fun filterCart() {
        _onlyInCart.value = !_onlyInCart.value
    }

    fun updateProductInCart(oldValue: Int, newValue: Int, product: Product) {
        _totalPrice.value += (newValue - oldValue) / product.quantitySold * product.price
        if (_totalPrice.value == 0f) {
            _onlyInCart.value = false
        }
    }

    private fun roundFloat(value: Float): Float = (value * 100.0f).roundToInt() / 100.0f

    class Factory(private val client: Client, private val repository: OrderRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PendingOrderViewModel::class.java)) {
                return PendingOrderViewModel(client, repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}