package vlad.dima.sales.ui.dashboard.manager_dashboard.notifications

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.User
import vlad.dima.sales.model.Notification
import vlad.dima.sales.ui.dashboard.common.notifications.NotificationsViewModel

class ManagerNotificationsViewModel(repository: UserRepository): NotificationsViewModel, ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = Firebase.firestore.collection("notifications")

    val isUserLoggedIn = MutableLiveData<Boolean>(true)
    private lateinit var currentUser: User

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()
    var items by mutableStateOf(listOf<Notification>())

    private val _isCreatingNewNotification = MutableStateFlow(false)
    val isCreatingNewNotification = _isCreatingNewNotification.asStateFlow()

    private val _isViewingNotification = MutableStateFlow<Intent?>(null)
    val isViewingNotification = _isViewingNotification.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                currentUser = user
                loadItems()
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        isUserLoggedIn.postValue(false)
    }

    override fun loadItems() = viewModelScope.launch(Dispatchers.IO) {
        _isRefreshing.emit(true)
        items = notificationsCollection.whereEqualTo("managerUID", currentUser.userUID).orderBy("createdDate", Query.Direction.DESCENDING).get().await().documents.toList().map {
            val notification = it.toObject(Notification::class.java)!!
            notification.id = it.id
            return@map notification
        }
        _isRefreshing.emit(false)
    }

    override fun viewNotification(intent: Intent) = viewModelScope.launch {
        _isViewingNotification.emit(intent)
        delay(100)
        _isViewingNotification.emit(null)
    }

    fun createNewNotification() = viewModelScope.launch {
        _isCreatingNewNotification.value = true
        delay(100)
        _isCreatingNewNotification.value = false
    }

    class Factory(private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManagerNotificationsViewModel::class.java)) {
                return ManagerNotificationsViewModel(repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}