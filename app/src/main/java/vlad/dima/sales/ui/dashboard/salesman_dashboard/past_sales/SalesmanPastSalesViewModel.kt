package vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.network.NetworkManager
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
    private val orderRepository: OrderRepository,
    private val networkManager: NetworkManager
) : ViewModel() {

    private val isHintHiddenKey = booleanPreferencesKey("isPastSalesHintHidden")
    private val _isHintHidden = settingsRepository.getSettingValue(isHintHiddenKey)
    var isHintHidden by mutableStateOf(false)
        private set

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private var currentUserWithDetails = MutableStateFlow(User())
    private val clientsCollection = Firebase.firestore.collection("clients")
    private val productsCollection = Firebase.firestore.collection("products")
    private val ordersCollection = Firebase.firestore.collection("orders")
    val networkStatus = networkManager.currentConnection

    // all products and clients are loaded at once so as not to send multiple requests for each order
    private val _scrollToClient = MutableStateFlow(0)
    val scrollToClient = _scrollToClient.asStateFlow()
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
            _isRefreshing.value = true
            val saleClients = clients
                .filter { client -> visibleOrders.count { order -> order.clientId == client.clientId } > 0 }
                .map { client ->
                    val saleOrders = visibleOrders.filter { order ->
                        order.clientId == client.clientId && falseDeletedOrders.find { it == order } == null
                    }
                        .map { order ->
                            var noStock = false
                            val saleProducts = orderProducts.filter { orderProduct ->
                                orderProduct.orderId == order.orderId
                            }
                                // some products may be deleted from the database in the meantime
                                .map { orderProduct ->
                                    products.find { it.productId == orderProduct.productId }
                                        ?.let { product ->
                                            if (product.stock < orderProduct.quantity) {
                                                noStock = true
                                            }
                                            SaleProduct(product, orderProduct.quantity)
                                        }
                                }
                                .sortedBy { it?.product?.productName }
                            SaleOrder(order, saleProducts.contains(null), noStock, saleProducts.filterNotNull())
                        }
                        .sortedByDescending { it.order.createdDate }
                    SaleClient(client, saleOrders)
                }
                .sortedBy { it.client.clientName }
            saleClients
        } else {
            emptyList()
        }.also {
            _isRefreshing.value = false
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    enum class UploadSaleState(var productCode: String = "") {
        Idle,
        Loading,
        StockInvalid,           // there are orders with products with insufficient stock
        ProductsInvalid,        // there are orders with products that are no longer available
        UploadSuccessful
    }

    private val _uploadState = MutableStateFlow(UploadSaleState.Idle)
    val uploadState = _uploadState.asStateFlow()
    private val _ordersWithInsufficientStock = MutableStateFlow(emptyList<Int>())
    val ordersWithInsufficientStock = _ordersWithInsufficientStock.asStateFlow()
    private val _ordersWithRemovedProducts = MutableStateFlow(emptyList<Int>())
    val ordersWithRemovedProducts = _ordersWithRemovedProducts.asStateFlow()
    private val _newOrdersRefresh = MutableStateFlow(false)                     // used to broadcast to the activity if there are new orders
    val newOrdersRefresh = _newOrdersRefresh.asStateFlow()



    //past sales
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _pastSales = MutableStateFlow(emptyList<Order>())
    val pastSaleClients = combine(
        _pastSales,
        _clients,
        products
    ) { pastSales, clients, products ->
        if (pastSales.isNotEmpty() && clients.isNotEmpty() && products.isNotEmpty()) {
            val clientIds = pastSales.map { it.clientId }.toSet()
            val saleClients = clients.filter { client -> clientIds.contains(client.clientId) }
                .map { client ->
                    val saleOrders = pastSales.filter { order ->
                        order.clientId == client.clientId
                    }
                        .map { order ->
                            val saleProducts = order.products.map { (productId, quantity) ->
                                products.find { it.productId == productId }?.let {
                                    return@let SaleProduct(it, quantity)
                                }
                            }
                                .sortedBy { it?.product?.productName }
                            // insufficient stock is not important for past sales
                            SaleOrder(order, saleProducts.contains(null), false, saleProducts.filterNotNull())
                        }
                        .sortedByDescending { it.order.createdDate }
                    SaleClient(client, saleOrders)
                }
                .sortedBy { it.client.clientName }
            saleClients
        } else {
            emptyList()
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    init {
        viewModelScope.launch {
            _isHintHidden.collect {
                isHintHidden = it == true
            }
        }
        viewModelScope.launch {
            networkStatus.combine(currentUserWithDetails) { status, currentUser ->
                if (status == NetworkManager.NetworkStatus.Available) {
                    if (products.value.isEmpty()) {
                        viewModelScope.launch(Dispatchers.IO) {
                            _isRefreshing.value = true
                            products.value =
                                productsCollection.get().await().documents.map { documentSnapshot ->
                                    documentSnapshot.toObject(Product::class.java)!!.apply {
                                        productId = documentSnapshot.id
                                    }
                                }
                        }
                    }
                    if (_pastSales.value.isEmpty()) {
                        loadPastSales()
                    }
                    if (currentUser.userUID != "") {
                        loadClients()
                    }
                }
            }
                .stateIn(viewModelScope)
        }
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserByUID(currentUser.uid).collect { user ->
                currentUserWithDetails.value = user
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


    private fun loadClients() = viewModelScope.launch(Dispatchers.IO) {
        _clients.value =
            clientsCollection.whereEqualTo("managerUID", currentUserWithDetails.value.managerUID).get()
                .await().documents.map { documentSnapshot ->
                    documentSnapshot.toObject(Client::class.java)!!.apply {
                        clientId = documentSnapshot.id
                    }
                }
    }

    private fun loadPastSales() = viewModelScope.launch(Dispatchers.IO) {
        _isRefreshing.value = true
        _pastSales.value = ordersCollection.whereEqualTo("salesmanUID", currentUser.uid).orderBy("createdDate", Query.Direction.DESCENDING).get().await().documents.map { documentSnapshot ->
            documentSnapshot.toObject(Order::class.java)!!
        }
        _isRefreshing.value = false
    }

    fun hideHint() = viewModelScope.launch {
        settingsRepository.updateSetting(isHintHiddenKey, true)
    }

    fun falseDeleteOrder(order: Order) {
        falseDeletedLocalOrders.value =
            falseDeletedLocalOrders.value.toMutableList().apply { add(order) }
    }

    fun deleteOrder(order: Order) = viewModelScope.launch(Dispatchers.IO) {
        orderRepository.deleteOrders(order)
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
        // used to update products stock
        val productQuantities = mutableMapOf<String, Int>()
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
                        _uploadState.value = UploadSaleState.StockInvalid.apply {
                            this.productCode = product.productCode
                        }
                        return@launch
                    }
                    productQuantities[productId] = productQuantity
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
        withContext(Dispatchers.IO) {
            productQuantities.forEach { (productId, quantity) ->
                productsCollection.document(productId)
                    .update("stock", FieldValue.increment((-quantity).toLong())).await()
            }
        }
        orderRepository.deleteOrders(*localOrders.value.toTypedArray())
        _uploadState.value = UploadSaleState.UploadSuccessful
        loadPastSales()
        _newOrdersRefresh.value = true
        delay(100)
        _newOrdersRefresh.value = false
    }

    fun dismissAlert() {
        if (_uploadState.value != UploadSaleState.Loading) {
            _uploadState.value = UploadSaleState.Idle
        }
    }

    fun refreshPastSales() = viewModelScope.launch(Dispatchers.IO) {
        _isRefreshing.value = true
        if (networkStatus.value == NetworkManager.NetworkStatus.Available) {
            products.value = productsCollection.get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(Product::class.java)!!.apply {
                    productId = documentSnapshot.id
                }
            }
            loadPastSales()
        }
        _isRefreshing.value = false
    }

    // used to scroll to the client with the pending order
    fun scrollTo(clientId: String) = viewModelScope.launch {
        _scrollToClient.value = pendingClients.value.indexOfFirst { it.client.clientId == clientId } + 1
        delay(100)
        _scrollToClient.value = 0
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository,
        private val networkManager: NetworkManager
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanPastSalesViewModel::class.java)) {
                return SalesmanPastSalesViewModel(
                    settingsRepository,
                    userRepository,
                    orderRepository,
                    networkManager
                ) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}