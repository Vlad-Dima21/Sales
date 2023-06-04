package vlad.dima.sales.ui.dashboard.manager_dashboard.productsStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.Product
import vlad.dima.sales.model.User
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.composables.ChartData
import java.util.Calendar
import kotlin.math.min

class ProductsStatsViewModel (
    private val networkManager: NetworkManager
): ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val productsCollection = Firebase.firestore.collection("products")
    private val ordersCollection = Firebase.firestore.collection("orders")
    private val usersCollection = Firebase.firestore.collection("users")
    private val storageReference = FirebaseStorage.getInstance().reference.child("product_images")

    val networkStatus = networkManager.currentConnection

    // orders made by salesmen in the team
    private val _salesmen = MutableStateFlow(emptyList<User>())
    private val _orders = MutableStateFlow(emptyList<Order>())
    private val _products = MutableStateFlow(emptyList<Product>())


    private val _selectedProductSelling = MutableStateFlow<Product?>(null)
    val selectedProductSelling = _selectedProductSelling.asStateFlow()

    private val _selectedProductProfitable = MutableStateFlow<Product?>(null)
    val selectedProductProfitable = _selectedProductProfitable.asStateFlow()

    private val _statsInterval = MutableStateFlow(7)
    val statsInterval = _statsInterval.asStateFlow()

    val topSellingProducts = combine(
        _products,
        _orders,
        _statsInterval
    ) { products, orders, statsInterval ->
        if (orders.isNotEmpty() && products.isNotEmpty()) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(statsInterval - 1))
            val topProducts = orders
                .filter { it.createdDate >= xDaysAgo.time }
                .map { it.products }
                .fold(mutableMapOf<String, Int>()) { acc, map ->
                    map.onEach {
                        if (acc.containsKey(it.key)) {
                            acc[it.key] = acc[it.key]!! + it.value
                        } else {
                            acc[it.key] = it.value
                        }
                    }
                    acc
                }
                .toList()
                .sortedByDescending { it.second }
                .let {
                    it.slice(0 until min(it.size, 5))
                }
                .mapNotNull { pair -> products.find { it.productId == pair.first} }
            if (topProducts.isNotEmpty()) {
                _selectedProductSelling.value = topProducts.first()
            }
            return@combine topProducts
        }
        emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val topSellingProductsChartData = combine(
        _orders,
        _selectedProductSelling,
        _statsInterval
    ) { orders, product, statsInterval ->
        if (orders.isNotEmpty() && product != null) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(statsInterval - 1))
            val daySales = orders.filter { it.createdDate >= xDaysAgo.time }.groupBy {
                val calendar = Calendar.getInstance()
                calendar.time =  it.createdDate
                "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}"
            }.mapValues { (_, orders) ->
                orders.fold(0) { acc, order ->
                    acc + (order.products[product.productId] ?: 0)
                }
            }
            return@combine ((statsInterval - 1) downTo 0).map {
                val itDaysAgo = Calendar.getInstance()
                itDaysAgo.add(Calendar.DAY_OF_MONTH, -it)
                val dayKey = "${itDaysAgo.get(Calendar.DAY_OF_MONTH)}.${itDaysAgo.get(Calendar.MONTH)}"
                ChartData(x = itDaysAgo.get(Calendar.DAY_OF_MONTH), y = daySales[dayKey] ?: 0)
            }
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val topProfitableProducts = combine(
        _products,
        _orders,
        _statsInterval
    ) { products, orders, interval ->
        if (orders.isNotEmpty() && products.isNotEmpty()) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val productsMap = products.associateBy { it.productId }
            val topProducts = orders
                .filter { it.createdDate >= xDaysAgo.time }
                .map { it.products }
                .fold(mutableMapOf<String, Float>()) { acc, map ->
                    map.onEach {
                        if (acc.containsKey(it.key)) {
                            acc[it.key] = acc[it.key]!! + it.value * (productsMap[it.key]?.price ?: 0f)
                        } else {
                            acc[it.key] = it.value * (productsMap[it.key]?.price ?: 0f)
                        }
                    }
                    acc
                }
                .toList()
                .sortedByDescending { it.second }
                .let {
                    it.slice(0 until min(it.size, 5))
                }
                .mapNotNull { pair -> productsMap[pair.first] }
            if (topProducts.isNotEmpty()) {
                _selectedProductProfitable.value = topProducts.first()
            }
            return@combine topProducts
        }
        emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val topProfitableChartData = combine(
        _orders,
        _selectedProductProfitable,
        _statsInterval
    ) { orders, product, interval ->
        if (orders.isNotEmpty() && product != null) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val daySales = orders.filter { it.createdDate >= xDaysAgo.time }.groupBy {
                val calendar = Calendar.getInstance()
                calendar.time =  it.createdDate
                "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}"
            }.mapValues { (_, orders) ->
                orders.fold(0f) { acc, order ->
                    acc + (order.products[product.productId] ?: 0) * product.price
                }
            }
            return@combine ((interval - 1) downTo 0).map {
                val itDaysAgo = Calendar.getInstance()
                itDaysAgo.add(Calendar.DAY_OF_MONTH, -it)
                val dayKey = "${itDaysAgo.get(Calendar.DAY_OF_MONTH)}.${itDaysAgo.get(Calendar.MONTH)}"
                ChartData(x = itDaysAgo.get(Calendar.DAY_OF_MONTH), y = daySales[dayKey]?.toInt() ?: 0)
            }
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadOrdersAndProducts()
    }

    fun loadOrdersAndProducts() = viewModelScope.launch(Dispatchers.IO) {
        if (networkStatus.value == NetworkManager.NetworkStatus.Available) {
            _isLoading.value = true
            _salesmen.value = usersCollection.whereEqualTo("managerUID", currentUser.uid).get()
                .await().documents.map { snapshot ->
                snapshot.toObject(User::class.java)!!
            }
            val salesmenUIDs = _salesmen.value.map { it.userUID }
            _orders.value = ordersCollection.whereIn("salesmanUID", salesmenUIDs).get()
                .await().documents.map { snapshot ->
                snapshot.toObject(Order::class.java)!!
            }
            _products.value = productsCollection.get().await().documents.map { snapshot ->
                snapshot.toObject(Product::class.java)!!.apply { productId = snapshot.id }
            }
            storageReference.listAll().await().items.forEach {
                val codeWithoutExtension = it.name.substring(0, it.name.lastIndexOf('.'))
                _products.value.find { it.productCode == codeWithoutExtension }?.productImageUri =
                    it.downloadUrl.await()
            }
            _isLoading.value = false
        }
    }

    fun changeInterval(interval: Int) {
        _statsInterval.value = interval
    }

    fun setSellingProduct(product: Product) {
        _selectedProductSelling.value = product
    }

    fun setProfitableProduct(product: Product) {
        _selectedProductProfitable.value = product
    }

    class Factory(private val networkManager: NetworkManager): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsStatsViewModel::class.java)) {
                return ProductsStatsViewModel(networkManager) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}