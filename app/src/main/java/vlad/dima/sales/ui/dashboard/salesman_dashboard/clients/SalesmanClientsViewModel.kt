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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.user.User

class SalesmanClientsViewModel(
    private val repository: UserRepository
): ViewModel() {

    private val clientCollection = Firebase.firestore.collection("clients")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUser: User

    private val _clients = MutableStateFlow(listOf<Client>())
    val clients: StateFlow<List<Client>>
        get() = _clients.asStateFlow()

    private val _isCreatingOrderIntent = MutableStateFlow<Client?>(null)
    val isCreatingOrderIntent: StateFlow<Client?>
        get() = _isCreatingOrderIntent.asStateFlow()

    var expandedClient = mutableStateOf(Client())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                currentUser = user
                loadClients()
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
        _isCreatingOrderIntent.emit(client)
        delay(100)
        _isCreatingOrderIntent.emit(null)
    }

    class Factory(private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanClientsViewModel::class.java)) {
                return SalesmanClientsViewModel(repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}