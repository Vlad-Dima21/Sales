package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.order.OrderProduct
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import kotlin.math.roundToInt

class PendingOrderViewModel(
    private val client: Client,
    private val repository: OrderRepository
) : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val productsCollection = Firebase.firestore.collection("products")
    private val storageReference = FirebaseStorage.getInstance().reference.child("product_images")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var _products = MutableStateFlow(listOf<ProductItemHolder>())

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

    val products = combine(
        _products,
        _onlyInStock,
        _onlyInCart,
        _totalPrice
    ) { products, onlyInStock, onlyInCart, _ ->
        var requestedProducts = products
        if (onlyInStock) {
            requestedProducts = requestedProducts.filter { it.product.stock > 0 }
        }
        if (onlyInCart) {
            requestedProducts = requestedProducts.filter { it.product.quantityAdded > 0 }
        }
        return@combine requestedProducts
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    init {
        loadProducts()
    }

    private fun loadProducts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)

        val loadedProducts =
            productsCollection.orderBy("productName").get().await().documents.map { snapshot ->
                val product = snapshot.toObject(Product::class.java)!!
                product.productId = snapshot.id
                ProductItemHolder(product) { oldValue, newValue, product ->
                    updateProductInCart(oldValue, newValue, product)
                }
            }
                .toMutableList()

        val productImagesReferences = storageReference.listAll().await().items.associate {
            val codeWithoutExtension = it.name.substring(0, it.name.lastIndexOf('.'))
            codeWithoutExtension to it.downloadUrl.await()
        }

        loadedProducts.forEach { productHolder ->
            productHolder.product.productImageUri = productImagesReferences[productHolder.product.productCode] ?: Uri.EMPTY
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

    private fun updateProductInCart(oldValue: Int, newValue: Int, product: Product) {
        _totalPrice.value += (newValue - oldValue) / product.quantitySold * product.price
    }

    private val _orderStatus = MutableStateFlow(OrderStatus.IDLE)
    val orderStatus = _orderStatus.asStateFlow()

    enum class OrderStatus {
        IDLE,
        LOADING,
        INVALID,
        SUCCEEDED,
        FAILED
    }

    fun saveOrder() = viewModelScope.launch {
        _orderStatus.value = OrderStatus.LOADING

        _products.value.find { it.validationMessage != null }?.let {
            _orderStatus.value = OrderStatus.INVALID
            delay(3000)
            _orderStatus.value = OrderStatus.IDLE
            return@launch
        }

        val newOrder = Order(
            clientId = client.clientId,
            salesmanUID = currentUser.uid
        )
        try {
            withContext(Dispatchers.IO) {
                val newId = repository.upsertOrder(newOrder)[0]
                repository.upsertOrderProducts(
                    _products.value.map {
                        OrderProduct(
                            orderId = newId.toInt(),
                            productId = it.product.productId,
                            quantity = it.product.quantityAdded
                        )
                    }
                )
                _orderStatus.value = OrderStatus.SUCCEEDED
            }
        } catch (e: Exception) {
            _orderStatus.value = OrderStatus.FAILED
            Log.e("PENDING_ORDER_ERROR", e.stackTraceToString())
            delay(3000)
            _orderStatus.value = OrderStatus.IDLE
        }
    }

    private fun roundFloat(value: Float): Float = (value * 100.0f).roundToInt() / 100.0f

    class ProductItemHolder(
        val product: Product,
        private val onQuantityChanged: (oldValue: Int, newValue: Int, product: Product) -> Unit
    ) {
        var quantityString by mutableStateOf("0")
            private set

        var validationMessage by mutableStateOf<Int?>(null)
            private set

        fun changeQuantity(selectedQuantity: String) {
            quantityString = selectedQuantity
            if (selectedQuantity.isNotEmpty()) {
                val value = selectedQuantity.trim().toInt()
                if (value % product.quantitySold != 0) {
                    validationMessage = R.string.ProductIncorrectValue
                    return
                }
                if (value > product.stock) {
                    validationMessage = R.string.ProductUnavailableStock
                    return
                }
                val oldValue = product.quantityAdded
                product.quantityAdded = value
                onQuantityChanged(oldValue, value, product)
                validationMessage = null
                return
            }
            validationMessage = R.string.FieldRequired
        }
    }
    class Factory(private val client: Client, private val repository: OrderRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PendingOrderViewModel::class.java)) {
                return PendingOrderViewModel(client, repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}

