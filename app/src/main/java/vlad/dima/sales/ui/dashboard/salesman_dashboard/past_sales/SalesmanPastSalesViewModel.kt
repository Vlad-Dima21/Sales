package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.repository.SettingsRepository
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.order.OrderProduct
import vlad.dima.sales.room.user.User
import vlad.dima.sales.ui.dashboard.common.products.Product
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy.SaleClient
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy.SaleOrder
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.order_hierarchy.SaleProduct
import java.util.Date

class SalesmanPastSalesViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val isHintHiddenKey = booleanPreferencesKey("isPastSalesHintHidden")
    private val _isHintHidden = settingsRepository.getSettingValue(isHintHiddenKey)
    var isHintHidden by mutableStateOf(false)
        private set

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private var currentUserWithDetails = User()
    private val clientsCollection = Firebase.firestore.collection("clients")
    private val productsCollection = Firebase.firestore.collection("products")
    private val ordersCollection = Firebase.firestore.collection("orders")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // all products are loaded at once so as not to send multiple requests for each order
    private val _clients = MutableStateFlow(emptyList<Client>())
    private val products = MutableStateFlow(emptyList<Product>())
    private val localOrders = MutableStateFlow(emptyList<Order>())
    private val falseDeletedLocalOrders = MutableStateFlow(emptyList<Order>())
    private val localOrderProducts = MutableStateFlow(emptyList<OrderProduct>())

    val pendingClients = combine(
        _clients,
        localOrders,
        localOrderProducts,
        products,
        falseDeletedLocalOrders
    ) { clients, orders, orderProducts, products, falseDeletedOrders ->
        val visibleOrders = orders.minus(falseDeletedOrders.toSet())
        if (clients.isNotEmpty() && visibleOrders.isNotEmpty()) {
            _isLoading.value = true
            val saleClients = clients
                .filter { client -> visibleOrders.count { order -> order.clientId == client.clientId } > 0 }
                .map { client ->
                    val saleOrders = visibleOrders.filter { order ->
                        order.clientId == client.clientId && falseDeletedOrders.find { it == order } == null
                    }
                        .map { order ->
                            val saleProducts = orderProducts.filter { orderProduct ->
                                orderProduct.orderId == order.orderId
                            }
                                // some products may be deleted from the database in the meantime
                                .mapNotNull { orderProduct ->
                                    products.find { it.productId == orderProduct.productId }
                                        ?.let { product ->
                                            SaleProduct(product, orderProduct.quantity)
                                        }
                                }
                                .sortedBy { it.product.productName }
                            SaleOrder(order, saleProducts)
                        }
                        .sortedByDescending { it.order.createdDate }
                    SaleClient(client, saleOrders)
                }
                .sortedBy { it.client.clientName }
            saleClients
        } else {
            emptyList()
        }.also {
            _isLoading.value = false
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    enum class UploadSaleState {
        Idle,
        Loading,
        StockInvalid,           // there are orders with products with insufficient stock
        ProductsInvalid,        // there are orders with products that are no longer available
        UploadSuccessful
    }

    private val _uploadState = MutableStateFlow(UploadSaleState.Idle)
    val uploadState = _uploadState.asStateFlow()
    private var _ordersWithInsufficientStock = MutableStateFlow(emptyList<Int>())
    val ordersWithInsufficientStock = _ordersWithInsufficientStock.asStateFlow()
    private var _ordersWithRemovedProducts = MutableStateFlow(emptyList<Int>())
    val ordersWithRemovedProducts = _ordersWithRemovedProducts.asStateFlow()


    init {
        viewModelScope.launch {
            _isHintHidden.collect {
                isHintHidden = it == true
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            products.value = productsCollection.get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(Product::class.java)!!.apply {
                    productId = documentSnapshot.id
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserByUID(currentUser.uid).collect { user ->
                currentUserWithDetails = user
                loadClients()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            orderRepository.getOrdersBySalesmanUID(currentUser.uid).collect { orders ->
                localOrders.value = orders
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            orderRepository.getOrderProductsBySalesmanUID(currentUser.uid)
                .collect { orderProducts ->
                    localOrderProducts.value = orderProducts
                }
        }
    }

    private suspend fun loadClients() {
        _clients.value =
            clientsCollection.whereEqualTo("managerUID", currentUserWithDetails.managerUID).get()
                .await().documents.map { documentSnapshot ->
                    documentSnapshot.toObject(Client::class.java)!!.apply {
                        clientId = documentSnapshot.id
                    }
                }
    }

    fun hideHint() = viewModelScope.launch {
        settingsRepository.updateSetting(isHintHiddenKey, true)
    }

    fun falseDeleteOrder(order: Order) {
        falseDeletedLocalOrders.value =
            falseDeletedLocalOrders.value.toMutableList().apply { add(order) }
    }

    fun deleteOrder(order: Order) = viewModelScope.launch(Dispatchers.IO) {
        orderRepository.deleteOrder(order)
        falseDeletedLocalOrders.value =
            falseDeletedLocalOrders.value.toMutableList().apply { remove(order) }
    }

    fun undoFalseDelete(order: Order) {
        falseDeletedLocalOrders.value =
            falseDeletedLocalOrders.value.toMutableList().apply { remove(order) }
    }

    fun placeLocalOrders() = viewModelScope.launch {
        _uploadState.value = UploadSaleState.Loading
        _ordersWithInsufficientStock.value = emptyList()
        _ordersWithRemovedProducts.value = emptyList()
        withContext(Dispatchers.IO) {
            products.value = productsCollection.get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(Product::class.java)!!.apply {
                    productId = documentSnapshot.id
                }
            }
        }
        localOrderProducts.value.groupBy { it.productId }
            .forEach { (productId, orderProducts) ->
                val product = products.value.find { it.productId == productId }
                if (product != null) {
                    val productQuantity =
                        orderProducts.fold(0) { sum: Int, orderProduct: OrderProduct ->
                            sum + orderProduct.quantity
                        }
                    if (productQuantity > product.stock) {
                        _ordersWithInsufficientStock.value =
                            _ordersWithInsufficientStock.value.toMutableList()
                                .apply { addAll(orderProducts.map { it.orderId }) }
                        _uploadState.value = UploadSaleState.StockInvalid
                        return@launch
                    }
                } else {
                    _ordersWithRemovedProducts.value =
                        _ordersWithRemovedProducts.value.toMutableList()
                            .apply { addAll(orderProducts.map { it.orderId }) }
                    _uploadState.value = UploadSaleState.ProductsInvalid
                    return@launch
                }
            }
        var maxOrderId = withContext(Dispatchers.IO) {
            ordersCollection.orderBy("orderId", Query.Direction.DESCENDING).limit(1).get()
                .await().documents.run {
                if (isEmpty()) {
                    return@run 100
                }
                return@run (get(0)["orderId"] as Long).toInt() + 1
            }
        }
        val groupedOrderProducts = localOrderProducts.value.groupBy { it.orderId }
        val fbOrders = localOrders.value.map { order ->
            order.copy(
                orderId = maxOrderId++,
                products = groupedOrderProducts[order.orderId]!!.associate { orderProduct ->
                    orderProduct.productId to orderProduct.quantity
                },
                createdDate = Date()
            )
        }
        fbOrders.forEach {
            withContext(Dispatchers.IO) {
                ordersCollection.add(it).await()
            }
        }
        _uploadState.value = UploadSaleState.UploadSuccessful
    }

    fun dismissAlert() {
        if (_uploadState.value != UploadSaleState.Loading) {
            _uploadState.value = UploadSaleState.Idle
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanPastSalesViewModel::class.java)) {
                return SalesmanPastSalesViewModel(
                    settingsRepository,
                    userRepository,
                    orderRepository
                ) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}