package vlad.dima.sales.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.model.User
import vlad.dima.sales.model.repository.SettingsRepository
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.network.NetworkManager
import java.lang.IllegalArgumentException

class SettingsViewModel(
    private val networkManager: NetworkManager,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val networkStatus = networkManager.currentConnection

    private val usersCollection = Firebase.firestore.collection("users")

    private val currentUserUID = FirebaseAuth.getInstance().currentUser!!.uid

    private val _currentUserWithInfo = MutableStateFlow<User?>(null)
    val currentUserWithInfo = _currentUserWithInfo.asStateFlow()

    private val _popupState = MutableStateFlow(Popup.Hidden)
    val popupState = _popupState.asStateFlow()

    private val _unassignedSalesmen = MutableStateFlow(emptyList<User>())
    val unassignedSalesmen = _unassignedSalesmen.asStateFlow()

    private val _salesmanPreferredImportance = settingsRepository.salesmanPreferredImportance()
    var salesmanPreferredImportance by mutableStateOf(SettingsRepository.NotificationImportance.Normal)
        private set

    // common setting
    private val _teamSalesmen = MutableStateFlow(emptyList<User>())
    val teamSalesmen = _teamSalesmen.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserByUID(currentUserUID).collect {
                _currentUserWithInfo.value = it
                if (it.managerUID == "") {
                    managerInit()
                } else {
                    salesmanInit(it.managerUID)
                }
            }
        }

        viewModelScope.launch {
            _salesmanPreferredImportance.collect {
                salesmanPreferredImportance = it
            }
        }
    }

    fun managerInit() {
        viewModelScope.launch(Dispatchers.IO) {
            _unassignedSalesmen.value =
                usersCollection.whereEqualTo("managerUID", "unassigned").get()
                    .await().documents.map {
                    it.toObject(User::class.java)!!
                }
                    .sortedBy { it.fullName }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _teamSalesmen.value = usersCollection.whereEqualTo("managerUID", currentUserUID).get()
                .await().documents.map {
                it.toObject(User::class.java)!!
            }
                .sortedBy { it.fullName }
        }
    }

    private fun salesmanInit(managerUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _teamSalesmen.value = usersCollection.where(
                Filter.or(
                    Filter.equalTo("managerUID", managerUID),
                    Filter.equalTo("userUID", managerUID)
                )
            ).get().await().documents.map {
                it.toObject(User::class.java)!!
            }
                .filter { it.userUID != currentUserUID }
                .sortedWith(compareBy({ it.managerUID }, { it.fullName }))
        }
    }

    fun logOutUser() {
        FirebaseAuth.getInstance().signOut()
    }

    fun setPopupState(popupState: Popup) {
        _popupState.value = popupState
    }

    fun addSalesmanToTeam(salesman: User) {
        viewModelScope.launch(Dispatchers.IO) {
            usersCollection.whereEqualTo("userUID", salesman.userUID).get().await().documents.let {
                if (it.isNotEmpty()) {
                    it.first().reference.update("managerUID", currentUserUID).await()
                }
            }
        }
    }

    fun setPreferredImportance(notificationImportance: SettingsRepository.NotificationImportance) {
        viewModelScope.launch {
            settingsRepository.setSalesmanPreferredImportance(notificationImportance)
        }
    }


    enum class Popup {
        Hidden,
        ManagerAddSalesmen,
        ManagerManageTeam,
        SalesmanViewTeamMembers
    }

    class Factory(
        private val networkManager: NetworkManager,
        private val userRepository: UserRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(networkManager, userRepository, settingsRepository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
}