package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.user.User

class SalesmanClientsViewModel(
    private val repository: UserRepository
): ViewModel() {

    private val clientCollection = Firebase.firestore.collection("clients")
    private val orderCollection = Firebase.firestore.collection("orders")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: User

    private val _clients = MutableStateFlow(listOf<Client>())

    private val _orders = MutableStateFlow(emptyList<Order>())
    val orders = _orders.asStateFlow()

    val clients = combine(
        _clients,
        _orders
    ) { clients, orders ->
        if (clients.isNotEmpty() && orders.isNotEmpty()) {
            val groupedOrders = orders.groupBy { it.clientId }
            return@combine clients.map { client ->
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

    var expandedClient = mutableStateOf(Client())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                currentUser = user
                loadClients()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _orders.value = orderCollection.whereEqualTo("salesmanUID", auth.currentUser!!.uid).get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(Order::class.java)!!
            }
        }
    }

    private fun loadClients() = viewModelScope.launch(Dispatchers.IO) {
        val managerUID = currentUser.managerUID.ifEmpty { currentUser.userUID }

        _clients.emit(clientCollection.whereEqualTo("managerUID", managerUID).get().await().map { document ->
              document.toObject(Client::class.java).apply {
                  clientId = document.id
              }
            }.toList()
        )
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

    class Factory(private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanClientsViewModel::class.java)) {
                return SalesmanClientsViewModel(repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}