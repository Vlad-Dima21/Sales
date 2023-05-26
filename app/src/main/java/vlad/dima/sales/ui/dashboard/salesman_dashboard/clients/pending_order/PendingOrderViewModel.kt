package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.net.Uri
import android.util.Log
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
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.OrderRepository
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.OrderProduct
import vlad.dima.sales.model.Product
import vlad.dima.sales.model.Client
import kotlin.math.roundToInt

class PendingOrderViewModel(
    private val clientId: String,
    private val orderId: Int?,
    private val repository: OrderRepository,
    private val networkManager: NetworkManager
) : ViewModel() {

    private val _client = MutableStateFlow(Client())
    val client = _client.asStateFlow()
    private val _order = MutableStateFlow<Order?>(null)
    private val _orderProducts = MutableStateFlow(emptyList<OrderProduct>())

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val clientsCollection = Firebase.firestore.collection("clients")
    private val productsCollection = Firebase.firestore.collection("products")
    private val storageReference = FirebaseStorage.getInstance().reference.child("product_images")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var _products = MutableStateFlow(listOf<ProductItemHolder>())
    private val networkStatus = networkManager.currentConnection

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
        viewModelScope.launch {
            networkStatus.collect { status ->
                if (_products.value.isEmpty() && status == NetworkManager.NetworkStatus.Available) {
                    loadClientAndProducts()
                }
            }
        }
        if (orderId != null) {
            combine(_order, _orderProducts) { order, orderProducts ->
                if (order != null && orderProducts.isNotEmpty()) {
                    orderProducts.forEach { orderProduct ->
                        _products.value.find { itemHolder ->
                            itemHolder.product.productId == orderProduct.productId
                        }
                            ?.apply {
                                changeQuantity(orderProduct.quantity.toString())
                            }
                    }
                    _isLoading.value = false
                }
            }
                .stateIn(viewModelScope, SharingStarted.Eagerly, Unit)
        }
    }

    private fun loadClientAndProducts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.emit(true)
        _client.value = clientsCollection.document(clientId).get().await().let { snapshot ->
            snapshot.toObject(Client::class.java)!!.apply {
                clientId = snapshot.id
            }
        }

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
            productHolder.product.productImageUri =
                productImagesReferences[productHolder.product.productCode] ?: Uri.EMPTY
        }

        _products.value = loadedProducts

        if (orderId != null) {
            loadOrder()
            loadOrderProducts()
        } else {
            _isLoading.emit(false)
        }
    }


    private fun loadOrder() = viewModelScope.launch(Dispatchers.IO) {
        repository.getOrderById(orderId!!).collect { order ->
            _order.value = order
        }
    }

    private fun loadOrderProducts() = viewModelScope.launch(Dispatchers.IO) {
        repository.getOrderProductsByOrderId(orderId!!).collect { orderProducts ->
            _orderProducts.value = orderProducts
        }
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
        try {
            if (orderId == null) {
                val newOrder = Order(
                    clientId = _client.value.clientId,
                    salesmanUID = currentUser.uid,
                    total = _totalPrice.value
                )
                withContext(Dispatchers.IO) {
                    val newId = repository.upsertOrder(newOrder)[0]
                    repository.upsertOrderProducts(
                        _products.value
                            .filter {
                                it.product.quantityAdded > 0
                            }
                            .map {
                                OrderProduct(
                                    orderId = newId.toInt(),
                                    productId = it.product.productId,
                                    quantity = it.product.quantityAdded
                                )
                            }
                    )
                    _orderStatus.value = OrderStatus.SUCCEEDED
                }
            } else {
                _order.value!!.total = _totalPrice.value
                withContext(Dispatchers.IO) {
                    repository.upsertOrder(_order.value!!)
                    repository.upsertOrderProducts(
                        _products.value
                            .filter {
                                it.product.quantityAdded > 0
                            }
                            .map {
                                OrderProduct(
                                    orderId = orderId,
                                    productId = it.product.productId,
                                    quantity = it.product.quantityAdded
                                )
                            }
                    )
                    _orderStatus.value = OrderStatus.SUCCEEDED
                }
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

    class Factory(
        private val clientId: String,
        private val orderId: Int?,
        private val repository: OrderRepository,
        private val networkManager: NetworkManager
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PendingOrderViewModel::class.java)) {
                return PendingOrderViewModel(clientId, orderId, repository, networkManager) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}

