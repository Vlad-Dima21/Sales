package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.model.Client
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.User
import vlad.dima.sales.ui.composables.SortState

class SalesmanClientsViewModel(
    private val repository: UserRepository,
    private val networkManager: NetworkManager
): ViewModel() {

    private val clientCollection = Firebase.firestore.collection("clients")
    private val orderCollection = Firebase.firestore.collection("orders")
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = MutableStateFlow(User())

    private val _clients = MutableStateFlow(listOf<Client>())
    private val _orders = MutableStateFlow(emptyList<Order>())
    val networkStatus = networkManager.currentConnection

    private val _sort = MutableStateFlow(SortState.None)
    val sort = _sort.asStateFlow()
    private val _search = MutableStateFlow("")
    var search by mutableStateOf("")

    val clients = combine(
        _clients,
        _orders,
        _sort,
        _search
    ) { clients, orders, sort, search ->
        if (clients.isNotEmpty()) {
            val groupedOrders = orders.groupBy { it.clientId }
            return@combine clients
                .let { cls ->
                    var finalClientOrder = cls
                    if (sort == SortState.Ascending) {
                        finalClientOrder = finalClientOrder.sortedBy { groupedOrders[it.clientId]?.size ?: 0 }
                    }
                    if (sort == SortState.Descending) {
                        finalClientOrder = finalClientOrder.sortedByDescending { groupedOrders[it.clientId]?.size ?: 0 }
                    }
                    if (search.isNotEmpty()) {
                        finalClientOrder = finalClientOrder.filter {
                            it.clientName.lowercase().contains(search.lowercase().trim())
                        }
                    }
                    finalClientOrder
                }
                .map { client ->
                ClientWithSaleInfo(
                    client = client,
                    numberOfSales = groupedOrders[client.clientId]?.size ?: 0
                )
            }
        }
        emptyList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _isCreatingOrderIntent = MutableStateFlow<String?>(null)
    val isCreatingOrderIntent = _isCreatingOrderIntent.asStateFlow()

    var expandedClient by mutableStateOf(Client())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                currentUser.value = user
            }
        }
        viewModelScope.launch {
            networkStatus.combine(currentUser) { status, user ->
                if (status == NetworkManager.NetworkStatus.Available && _clients.value.isEmpty()) {
                    loadClients()
                    if (user.userUID != "") {
                        loadOrders()
                    }
                }
            }
                .stateIn(viewModelScope, SharingStarted.Eagerly, Unit)
        }
    }

    private fun loadClients() = viewModelScope.launch(Dispatchers.IO) {
        val managerUID = currentUser.value.managerUID.ifEmpty { currentUser.value.userUID }
        _clients.emit(clientCollection.whereEqualTo("managerUID", managerUID).orderBy("clientName", Query.Direction.ASCENDING).get().await().map { document ->
              document.toObject(Client::class.java).apply {
                  clientId = document.id
              }
            }.toList()
        )
    }

    fun loadOrders() = viewModelScope.launch(Dispatchers.IO) {
        _orders.value = orderCollection.whereEqualTo("salesmanUID", auth.currentUser!!.uid).get().await().documents.map { documentSnapshot ->
            documentSnapshot.toObject(Order::class.java)!!
        }
    }

    fun toggleSort() {
        _sort.value = _sort.value.nextState()
    }

    fun changeSearch() {
        _search.value = search
    }

    fun startCreatingOrder(client: Client) = viewModelScope.launch {
        _isCreatingOrderIntent.emit(client.clientId)
        delay(100)
        _isCreatingOrderIntent.emit(null)
    }

    data class ClientWithSaleInfo(
        val client: Client,
        val numberOfSales: Int
    )

    class Factory(private val repository: UserRepository, private val networkManager: NetworkManager): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanClientsViewModel::class.java)) {
                return SalesmanClientsViewModel(repository, networkManager) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}