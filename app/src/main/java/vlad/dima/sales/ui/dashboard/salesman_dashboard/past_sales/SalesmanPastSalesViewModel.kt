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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
        if (clients.isNotEmpty() && orders.minus(falseDeletedOrders.toSet()).isNotEmpty()) {
            _isLoading.value = true
            val saleClients = clients
                .filter { client -> orders.count { order -> order.clientId == client.clientId } > 0 }
                .map { client ->
                val saleOrders = orders.filter { order ->
                    order.clientId == client.clientId && falseDeletedOrders.find {it == order} == null
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
        falseDeletedLocalOrders.value = falseDeletedLocalOrders.value.toMutableList().apply { add(order) }
    }

    fun deleteOrder(order: Order) = viewModelScope.launch(Dispatchers.IO) {
        orderRepository.deleteOrder(order)
        falseDeletedLocalOrders.value = falseDeletedLocalOrders.value.toMutableList().apply { remove(order) }
    }

    fun undoFalseDelete(order: Order) {
        falseDeletedLocalOrders.value = falseDeletedLocalOrders.value.toMutableList().apply { remove(order) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val userRepository: UserRepository,
        private val orderRepository: OrderRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanPastSalesViewModel::class.java)) {
                return SalesmanPastSalesViewModel(settingsRepository, userRepository, orderRepository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}