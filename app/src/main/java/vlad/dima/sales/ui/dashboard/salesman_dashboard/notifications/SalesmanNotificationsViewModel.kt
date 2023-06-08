package vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.User
import vlad.dima.sales.model.Notification
import vlad.dima.sales.model.repository.SettingsRepository
import vlad.dima.sales.ui.dashboard.common.notifications.NotificationsViewModel

class SalesmanNotificationsViewModel(
    private val repository: UserRepository,
    private val networkManager: NetworkManager,
    private val settingsRepository: SettingsRepository
): NotificationsViewModel, ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = Firebase.firestore.collection("notifications")

    private lateinit var currentUser: User

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _preferredImportance = settingsRepository.salesmanPreferredImportance()
//    var items by mutableStateOf(listOf<Notification>())
    private val _notifications = MutableStateFlow(emptyList<Notification>())
    val notifications = combine(
        _notifications,
        _preferredImportance
    ) { notifications, importance ->
        notifications.filter { it.importance >= importance.number }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _isViewingNotificationIntent = MutableStateFlow<Intent?>(null)
    val networkStatus = networkManager.currentConnection
    val isViewingNotificationIntent = combine(
        _isViewingNotificationIntent,
        networkStatus
    ) { isViewingNotificationIntent, networkStatus ->
        if (networkStatus != NetworkManager.NetworkStatus.Available) {
            null
        } else {
            isViewingNotificationIntent
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                currentUser = user
                loadItems()
            }
        }

        viewModelScope.launch {
            networkStatus.collect { status ->
                if (::currentUser.isInitialized && status == NetworkManager.NetworkStatus.Available && _notifications.value.isEmpty()) {
                    loadItems()
                }
            }
        }
    }

    override fun loadItems() = viewModelScope.launch(Dispatchers.IO) {
        if (networkStatus.value == NetworkManager.NetworkStatus.Available) {
            _isRefreshing.emit(true)
            _notifications.value = notificationsCollection.whereEqualTo("managerUID", currentUser.managerUID)
                .orderBy("createdDate", Query.Direction.DESCENDING).get().await().documents.toList()
                .map {
                    val notification = it.toObject(Notification::class.java)!!
                    notification.id = it.id
                    return@map notification
                }
            _isRefreshing.emit(false)
        }
    }

    override fun viewNotification(intent: Intent) = viewModelScope.launch {
        _isViewingNotificationIntent.emit(intent)
        delay(100)
        _isViewingNotificationIntent.emit(null)
    }
    class Factory(
        private val repository: UserRepository,
        private val networkManager: NetworkManager,
        private val settingsRepository: SettingsRepository
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanNotificationsViewModel::class.java)) {
               return SalesmanNotificationsViewModel(repository, networkManager, settingsRepository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}
