package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.room.user.User

class SalesmanClientsViewModel: ViewModel() {

    private val clientCollection = Firebase.firestore.collection("clients")
    var currentUserState by mutableStateOf(User())

    private val _clients = MutableStateFlow(listOf<Client>())
    val clients: StateFlow<List<Client>>
        get() = _clients.asStateFlow()

    private val _isCreatingOrderIntent = MutableStateFlow<Client?>(null)
    val isCreatingOrderIntent: StateFlow<Client?>
        get() = _isCreatingOrderIntent.asStateFlow()

    var expandedClient = mutableStateOf(Client())

    fun loadClients() = CoroutineScope(Dispatchers.IO).launch {
        val managerUID = currentUserState.managerUID.ifEmpty { currentUserState.userUID }

        _clients.emit(clientCollection.whereEqualTo("managerUID", managerUID).get().await().map { document ->
              document.toObject(Client::class.java)
            }.toList()
        )
    }

    fun startCreatingOrder(client: Client) = viewModelScope.launch {
        _isCreatingOrderIntent.emit(client)
    }
}