package vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.user.User
import vlad.dima.sales.ui.dashboard.common.notifications.Notification

class SalesmanNotificationsViewModel(val repository: UserRepository): ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = Firebase.firestore.collection("notifications")

    val isUserLoggedIn = MutableLiveData<Boolean>(true)
    val currentUserLD = repository.getUserByUID(auth.currentUser!!.uid)
    var currentUserSate by mutableStateOf(User())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()
    var items by mutableStateOf(listOf<Notification>())

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        isUserLoggedIn.postValue(false)
    }

    fun loadItems() = CoroutineScope(Dispatchers.IO).launch {
        _isRefreshing.emit(true)
        items = notificationsCollection.whereEqualTo("managerUID", currentUserSate.managerUID).get().await().documents.toList().map {
            it.toObject(Notification::class.java)!!
        }
        _isRefreshing.emit(false)
    }
    class Factory(private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SalesmanNotificationsViewModel::class.java)) {
               return SalesmanNotificationsViewModel(repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}
