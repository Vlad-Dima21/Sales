package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.user.User
import vlad.dima.sales.ui.dashboard.common.notifications.Notification

class NotificationChatViewModel(private val notificationId: String, repository: UserRepository): ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = Firebase.firestore.collection("notifications")
    private val notificationMessagesCollection = Firebase.firestore.collection("notificationMessages")

    val currentUserLD = repository.getUserByUID(auth.currentUser?.uid ?: "")
    var currentUserState = MutableStateFlow(User())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private val _currentNotification = MutableStateFlow(Notification())
    val currentNotification: StateFlow<Notification>
        get() = _currentNotification.asStateFlow()
    var notificationDeleted by mutableStateOf(false)

    private val _messages = MutableStateFlow(listOf<NotificationMessage>())
    val messages: StateFlow<List<NotificationMessage>>
        get() = _messages.asStateFlow()

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?>
        get() = _error.asStateFlow()

    var message by mutableStateOf("")

    init {
        loadNotification()
        loadMessages()
    }

    private fun loadNotification() = CoroutineScope(Dispatchers.IO).launch {
        _isRefreshing.emit(true)
        notificationsCollection.document(notificationId).addSnapshotListener { notification, error ->
            viewModelScope.launch {
                if (error != null) {
                    _error.emit(R.string.NotificationMessageError)
                    Log.e("NOTIFICATION_MESSAGE", error.stackTraceToString())
                    return@launch
                }

                if (!notification!!.exists()) {
                    withContext(Dispatchers.IO) {
                        notificationMessagesCollection.whereEqualTo("notificationId", notificationId).get().await().forEach {
                            it.reference.delete()
                        }
                    }
                    _error.emit(R.string.NotificationDeleted)
                } else {
                    notification.toObject(Notification::class.java)
                        ?.let { _currentNotification.emit(it) }
                }
            }
        }
        _isRefreshing.emit(false)
    }

    private fun loadMessages() {
        notificationMessagesCollection
            .whereEqualTo("notificationId", notificationId)
            .orderBy("sendDate", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, snapshotException ->
                snapshotException?.let {
                    viewModelScope.launch {
                        _error.emit(R.string.NotificationMessageError)
                        Log.e("NOTIFICATION_MESSAGE", snapshotException.stackTraceToString())
                    }
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    _isRefreshing.emit(true)
                    querySnapshot?.let { query ->
                        _messages.emit(
                            query.map {
                                it.toObject(NotificationMessage::class.java)
                            }
                        )
                    }
                    _isRefreshing.emit(false)
                }
            }
    }

    fun sendMessage() = CoroutineScope(Dispatchers.IO).launch {
        notificationMessagesCollection.add(NotificationMessage(
            notificationId = notificationId,
            authorName = currentUserState.value.fullName,
            authorUID = currentUserState.value.userUID,
            message = message
        ))
        message = ""
    }

    fun deleteMessage() = CoroutineScope(Dispatchers.IO).launch {
        notificationsCollection.document(notificationId).delete()
    }


    class Factory(private val notificationId: String, private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationChatViewModel::class.java)) {
                return NotificationChatViewModel(notificationId, repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}