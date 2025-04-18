package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.model.Notification
import vlad.dima.sales.model.NotificationMessage
import vlad.dima.sales.model.User
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.utils.DateTime
import java.util.Date

class NotificationChatViewModel(
    private val notificationId: String,
    private val repository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = Firebase.firestore.collection("notifications")
    private val notificationMessagesCollection =
        Firebase.firestore.collection("notificationMessages")
    private lateinit var notificationSnapshotListener: ListenerRegistration
    private lateinit var messagesSnapshotListener: ListenerRegistration

    private val _currentUser = MutableStateFlow(User())
    var currentUser = _currentUser.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private val _currentNotification = MutableStateFlow(Notification())
    val currentNotification: StateFlow<Notification>
        get() = _currentNotification.asStateFlow()

    val currentFormattedDay = DateTime.getDate(Date())
    private val _messages = MutableStateFlow(listOf<NotificationMessage>())
    val messages = _messages.asStateFlow()
    val groupedMessages = _messages.map { messages ->
        messages.groupBy {
            DateTime(it.sendDate)
        }
            .toList()
            .sortedBy { it.first.date }
            .map { (dateTime, messages) ->
                DateTime.getDate(dateTime.date) to messages
            }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?>
        get() = _error.asStateFlow()

    var message by mutableStateOf("")

    var search by mutableStateOf("")
    private var queryResults = mutableListOf<Pair<String, Int>>()
    var currentResult = -1
    var currentResultPosition = MutableStateFlow(Pair("", -1))
    var noResults by mutableStateOf(-1)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserByUID(auth.currentUser!!.uid).collect { user ->
                _currentUser.value = user
            }
        }
        loadNotification()
        loadMessages()
    }

    private fun loadNotification() = viewModelScope.launch(Dispatchers.IO) {
        _isRefreshing.emit(true)
        notificationSnapshotListener = notificationsCollection.document(notificationId)
            .addSnapshotListener { notification, error ->
                viewModelScope.launch {
                    if (error != null) {
                        _error.emit(R.string.NotificationMessageError)
                        Log.e("NOTIFICATION_MESSAGE", error.stackTraceToString())
                        return@launch
                    }
                    if (!notification!!.exists()) {
                        withContext(Dispatchers.IO) {
                            notificationMessagesCollection.whereEqualTo(
                                "notificationId",
                                notificationId
                            ).get().await().forEach {
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
        messagesSnapshotListener = notificationMessagesCollection
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
                                it.toObject(NotificationMessage::class.java).apply { notificationMessageId = it.id }
                            }
                        )
                    }
                    _isRefreshing.emit(false)
                }
            }
    }

    fun sendMessage() = viewModelScope.launch(Dispatchers.IO) {
        notificationMessagesCollection.add(
            NotificationMessage(
                notificationId = notificationId,
                authorName = currentUser.value.fullName,
                authorUID = currentUser.value.userUID,
                message = message
            )
        )
        message = ""
    }

    fun deleteNotification() = viewModelScope.launch(Dispatchers.IO) {
        notificationsCollection.document(notificationId).delete()
    }

    override fun onCleared() {
        super.onCleared()
        notificationSnapshotListener.remove()
        messagesSnapshotListener.remove()
    }

    fun searchMessages() = viewModelScope.launch {
        currentResult = -1
        currentResultPosition.value = Pair("", -1)
        delay(100)
        queryResults = mutableListOf()
        noResults = -1
        if (search.isEmpty()) {
            return@launch
        }
        groupedMessages.value.forEachIndexed { index, (_, messages) ->
            messages.forEachIndexed {  messageIndex, message ->
                if (message.message.lowercase().contains(search.lowercase().trim())) {
                    queryResults.add(
                        Pair(
                            message.notificationMessageId,
                            1 + index + messageIndex + groupedMessages.value.subList(0, index).fold(0) { acc, (_, list) -> acc + list.size}
                        )
                    )
                }
            }
        }
        if (queryResults.isNotEmpty()) {
            currentResult = 0
            currentResultPosition.value = queryResults[0]
        }
        noResults = queryResults.size
    }

    fun goToSearchResult(position: Int) = viewModelScope.launch {
        currentResultPosition.value = Pair("", -1)
        delay(100)
        if (queryResults.isNotEmpty()) {
            if (currentResult + position < 0) {
                currentResult = queryResults.size - 1
            } else {
                currentResult = (currentResult + position) % queryResults.size
            }
            currentResultPosition.value = queryResults[currentResult]
        }
    }

    fun resetSearch() {
        search = ""
        currentResult = -1
        currentResultPosition.value = Pair("", -1)
        queryResults = mutableListOf()
        noResults = -1
    }

    class Factory(private val notificationId: String, private val repository: UserRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationChatViewModel::class.java)) {
                return NotificationChatViewModel(notificationId, repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}