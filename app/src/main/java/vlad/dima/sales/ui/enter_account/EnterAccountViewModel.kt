package vlad.dima.sales.ui.enter_account

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.user.User

class EnterAccountViewModel(private val repository: UserRepository, private val networkManager: NetworkManager): ViewModel() {
    
    private val AUTH_TAG = "Firebase_auth_error"

    private val emailRegex = Regex(
        pattern = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$",
        option = RegexOption.IGNORE_CASE
    )
    var emailFieldState by mutableStateOf("")
    var fullNameFieldState by mutableStateOf("")
    var passwordFieldState by mutableStateOf("")

    private val _inputError = MutableStateFlow(InvalidFields.None)
    val inputError = _inputError.asStateFlow()
    private val errorDelay = 2000L

    private val _actionResult = MutableStateFlow<AccountStatus?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val userCollectionRef = Firebase.firestore.collection("users")

    private val operationInProgress = MutableStateFlow(false)
    val areButtonsEnabled = combine(
        operationInProgress,
        networkManager.currentConnection
    ) { operationInProgress, connection ->
        if (operationInProgress || connection != NetworkManager.NetworkStatus.Available) {
            return@combine false
        }
        true
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun signUpUser() {
        _inputError.value = InvalidFields.None
        operationInProgress.value = true
        if (emailFieldState.isNotEmpty() && fullNameFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()) {
            if (!emailRegex.matches(emailFieldState)) {
                viewModelScope.launch {
                    _inputError.value = InvalidFields.Email
                    _actionResult.value = AccountStatus(R.string.InvalidEmail, false)
                    operationInProgress.value = false
                    delay(errorDelay)
                    _actionResult.value = null
                }
                return
            }
            viewModelScope.launch {
                try {
                    auth.createUserWithEmailAndPassword(emailFieldState, passwordFieldState).await()
                    auth.currentUser?.let { user ->
                        withContext(Dispatchers.IO) {
                            val newUser = User(
                                fullName = fullNameFieldState,
                                userUID = user.uid
                            )
                            try {
                                userCollectionRef.add(
                                    newUser
                                ).await()
                                repository.upsertUser(newUser)
                            } catch (e: Exception) {
                                Log.d(AUTH_TAG, e.stackTraceToString())
                                _actionResult.value = AccountStatus(R.string.SystemError, false)
                                auth.currentUser?.delete()
                                auth.signOut()
                            }
                        }
                    }
                    _actionResult.value = when {
                        isLoggedIn() -> AccountStatus(R.string.SignUpSuccessful, true)
                        else -> AccountStatus(R.string.SignUpUnsuccessful, false)
                    }
                } catch (e: FirebaseAuthUserCollisionException) {
                    _actionResult.value = AccountStatus(R.string.EmailAlreadyInUse, false)
                    _inputError.value = InvalidFields.Email
                }  catch (e: FirebaseAuthInvalidCredentialsException) {
                    _actionResult.value = AccountStatus(R.string.PasswordIsTooWeak, false)
                    _inputError.value = InvalidFields.Password
                } catch (e: Exception) {
                    Log.e(AUTH_TAG, e.stackTraceToString())
                    _actionResult.value = AccountStatus(R.string.SystemError, false)
                }
                operationInProgress.value = false
                delay(errorDelay)
                _actionResult.value = null
            }
        } else {
            operationInProgress.value = false
            _actionResult.value = AccountStatus(R.string.FillRequiredFields, false)
            _inputError.value = InvalidFields.All
            viewModelScope.launch {
                delay(errorDelay)
                _actionResult.value = null
            }
        }
    }

    fun loginUser() {
        operationInProgress.value = true
        if (emailFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()) {
            if (!emailRegex.matches(emailFieldState)) {
                viewModelScope.launch {
                    _inputError.value = InvalidFields.Email
                    _actionResult.value = AccountStatus(R.string.InvalidEmail, false)
                    operationInProgress.value = false
                    delay(errorDelay)
                    _actionResult.value = null
                }
                return
            }
            viewModelScope.launch {
                try {
                    auth.signInWithEmailAndPassword(emailFieldState, passwordFieldState).await()
                    auth.currentUser?.let { user ->
                        withContext(Dispatchers.IO) {
                            val db = Firebase.firestore.collection("users")
                            val userFromDb = db.whereEqualTo("userUID", user.uid).get().await().documents[0].toObject(User::class.java)
                            val newUser = User(
                                fullName = userFromDb!!.fullName,
                                userUID = user.uid,
                                managerUID = userFromDb!!.managerUID
                            )
                            repository.upsertUser(newUser)
                        }
                    }
                    _actionResult.value =
                        when {
                            isLoggedIn() -> AccountStatus(R.string.LogInSuccessful, true)
                            else -> AccountStatus(R.string.LogInUnsuccessfull, false)
                        }
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    _actionResult.value = AccountStatus(R.string.PasswordIsTooWeak, false)
                    _inputError.value = InvalidFields.Password
                } catch (e: FirebaseAuthInvalidUserException) {
                    _actionResult.value = AccountStatus(R.string.InvalidCredentials, false)
                    _inputError.value = InvalidFields.All
                } catch (e: Exception) {
                    auth.signOut()
                    Log.e(AUTH_TAG, e.stackTraceToString())
                    _actionResult.value = AccountStatus(R.string.SystemError, false)
                }
                operationInProgress.value = false
                delay(errorDelay)
                _actionResult.value = null
            }
        } else {
            operationInProgress.value = false
            _actionResult.value = AccountStatus(R.string.FillRequiredFields, false)
            _inputError.value = InvalidFields.All
            viewModelScope.launch {
                delay(errorDelay)
                _actionResult.value = null
            }
        }
    }

    fun switchPage() {
        _inputError.value = InvalidFields.None
        emailFieldState = ""
        fullNameFieldState = ""
        passwordFieldState = ""
    }

    enum class InvalidFields {
        None,
        Password,
        Email,
        All
    }

    class Factory(private val repository: UserRepository, private val networkManager: NetworkManager): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EnterAccountViewModel::class.java)) {
                return EnterAccountViewModel(repository, networkManager) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}