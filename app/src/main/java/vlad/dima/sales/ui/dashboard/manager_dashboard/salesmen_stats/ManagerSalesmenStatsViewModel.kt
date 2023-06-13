package vlad.dima.sales.ui.dashboard.manager_dashboard.salesmen_stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.User
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.composables.ChartData
import java.lang.Math.min
import java.util.Calendar

class ManagerSalesmenStatsViewModel(
    networkManager: NetworkManager
) : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val ordersCollection = Firebase.firestore.collection("orders")
    private val usersCollection = Firebase.firestore.collection("users")

    val networkStatus = networkManager.currentConnection

    private val _salesmen = MutableStateFlow(emptyList<User>())
    private val _orders = MutableStateFlow(emptyList<Order>())

    private val _selectedSalesmanSelling = MutableStateFlow<User?>(null)
    val selectedSalesmanSelling = _selectedSalesmanSelling.asStateFlow()

    private val _selectedSalesmanProfitable = MutableStateFlow<User?>(null)
    val selectedSalesmanProfitable = _selectedSalesmanProfitable.asStateFlow()

    private val _statsInterval = MutableStateFlow(7)
    val statsInterval = _statsInterval.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val salesmenBySales = combine(
        _orders,
        _salesmen,
        _statsInterval
    ) { orders, salesmen, interval ->
        if (orders.isNotEmpty() && salesmen.isNotEmpty()) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val salesmenMap = salesmen.associateBy { it.userUID }
            val topSalesmen = orders
                .filter { it.createdDate >= xDaysAgo.time }
                .groupBy { it.salesmanUID }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .let {
                    it.slice(0 until min(it.size, 5))
                }
                .mapNotNull { salesmenMap[it.first] }
            if (topSalesmen.isNotEmpty()) {
                _selectedSalesmanSelling.value = topSalesmen.first()
            }
            return@combine topSalesmen
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val salesmenBySalesChartData = combine(
        _orders,
        _selectedSalesmanSelling,
        _statsInterval
    ) { orders, salesman, interval ->
        if (orders.isNotEmpty() && salesman != null) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val daySales =
                orders.filter { it.createdDate >= xDaysAgo.time && it.salesmanUID == salesman.userUID }
                    .groupBy {
                        val calendar = Calendar.getInstance()
                        calendar.time = it.createdDate
                        "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}"
                    }
                    .mapValues {
                        it.value.size
                    }
            return@combine ((interval - 1) downTo 0).map {
                val itDaysAgo = Calendar.getInstance()
                itDaysAgo.add(Calendar.DAY_OF_MONTH, -it)
                val dayKey =
                    "${itDaysAgo.get(Calendar.DAY_OF_MONTH)}.${itDaysAgo.get(Calendar.MONTH)}"
                ChartData(x = itDaysAgo.get(Calendar.DAY_OF_MONTH), y = daySales[dayKey] ?: 0)
            }
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val salesmenByProfit = combine(
        _orders,
        _salesmen,
        _statsInterval
    ) { orders, salesmen, interval ->
        if (orders.isNotEmpty() && salesmen.isNotEmpty()) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val salesmenMap = salesmen.associateBy { it.userUID }
            val topSalesmen = orders
                .filter { it.createdDate >= xDaysAgo.time }
                .groupBy { it.salesmanUID }
                .mapValues {
                    it.value.fold(0f) { acc, order ->
                        acc + order.total
                    }
                }
                .toList()
                .sortedByDescending { it.second }
                .let {
                    it.slice(0 until min(it.size, 5))
                }
                .mapNotNull { salesmenMap[it.first] }
            if (topSalesmen.isNotEmpty()) {
                _selectedSalesmanProfitable.value = topSalesmen.first()
            }
            return@combine topSalesmen
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val salesmenByProfitChartData = combine(
        _orders,
        _selectedSalesmanSelling,
        _statsInterval
    ) { orders, salesman, interval ->
        if (orders.isNotEmpty() && salesman != null) {
            val xDaysAgo = Calendar.getInstance()
            xDaysAgo.add(Calendar.DAY_OF_YEAR, -(interval - 1))
            val daySales =
                orders.filter { it.createdDate >= xDaysAgo.time && it.salesmanUID == salesman.userUID }
                    .groupBy {
                        val calendar = Calendar.getInstance()
                        calendar.time = it.createdDate
                        "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}"
                    }
                    .mapValues {
                        it.value.fold(0f) { acc, order ->
                            acc + order.total
                        }
                    }
            return@combine ((interval - 1) downTo 0).map {
                val itDaysAgo = Calendar.getInstance()
                itDaysAgo.add(Calendar.DAY_OF_MONTH, -it)
                val dayKey =
                    "${itDaysAgo.get(Calendar.DAY_OF_MONTH)}.${itDaysAgo.get(Calendar.MONTH)}"
                ChartData(x = itDaysAgo.get(Calendar.DAY_OF_MONTH), y = daySales[dayKey]?.toInt() ?: 0)
            }
        }
        return@combine emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadOrdersAndSalesmen()
    }

    fun loadOrdersAndSalesmen() = viewModelScope.launch(Dispatchers.IO) {
        if (networkStatus.value == NetworkManager.NetworkStatus.Available) {
            _isLoading.value = true
            _salesmen.value = usersCollection.whereEqualTo("managerUID", currentUser.uid).get()
                .await().documents.map { snapshot ->
                    snapshot.toObject(User::class.java)!!
                }
            val salesmenUIDs = _salesmen.value.map { it.userUID }
            if (salesmenUIDs.isNotEmpty()) {
                _orders.value = ordersCollection.whereIn("salesmanUID", salesmenUIDs).get()
                    .await().documents.map { snapshot ->
                        snapshot.toObject(Order::class.java)!!
                    }
            }
            _isLoading.value = false
        }
    }

    fun changeInterval(interval: Int) {
        _statsInterval.value = interval
    }

    fun setSellingSalesman(salesman: User) {
        _selectedSalesmanSelling.value = salesman
    }

    fun setProfitableSalesman(salesman: User) {
        _selectedSalesmanProfitable.value = salesman
    }

    class Factory(private val networkManager: NetworkManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManagerSalesmenStatsViewModel::class.java)) {
                return ManagerSalesmenStatsViewModel(networkManager) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}