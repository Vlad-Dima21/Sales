package vlad.dima.sales.ui.enter_account

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.user.User

class EnterAccountViewModel(val repository: UserRepository): ViewModel() {

    class Factory(private val repository: UserRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EnterAccountViewModel::class.java)) {
                return EnterAccountViewModel(repository) as T
            }
            throw IllegalArgumentException("Wrong viewModel type")
        }
    }
    
    private val AUTH_TAG = "Firebase_auth_error"
    
    var emailFieldState by mutableStateOf("")
    var fullNameFieldState by mutableStateOf("")
    var passwordFieldState by mutableStateOf("")
    var inputError by mutableStateOf(false)

    val actionResult: MutableLiveData<AccountStatus> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()
    private val userCollectionRef = Firebase.firestore.collection("users")

    fun signUpUser() {
        inputError = false

        if (emailFieldState.isNotEmpty() && fullNameFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()) {
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
                                )
                            } catch (e: Exception) {
                                Log.d(AUTH_TAG, e.stackTraceToString())
                                actionResult.postValue(AccountStatus(R.string.SystemError, false))
                                auth.signOut()
                            }
                            repository.upsertUser(newUser)
                        }
                    }
                    actionResult.postValue(when {
                        isLoggedIn() -> AccountStatus(R.string.SignUpSuccessful, true)
                        else -> AccountStatus(R.string.SignUpUnsuccessful, false)
                    })
                } catch (e: FirebaseAuthUserCollisionException) {
                    actionResult.postValue(AccountStatus(R.string.EmailAlreadyInUse, false))
                    inputError = true
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    actionResult.postValue(AccountStatus(R.string.InvalidCredentials, false))
                    inputError = true
                } catch (e: Exception) {
                    Log.e(AUTH_TAG, e.stackTraceToString())
                    actionResult.postValue(AccountStatus(R.string.SystemError, false))
                }
            }
        } else {
            actionResult.postValue(AccountStatus(R.string.FillRequiredFields, false))
            inputError = true
        }
    }

    fun loginUser() {
        if (emailFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()) {
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
                    actionResult.postValue(
                        when {
                            isLoggedIn() -> AccountStatus(R.string.LogInSuccessful, true)
                            else -> AccountStatus(R.string.LogInUnsuccessfull, false)
                        }
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    actionResult.postValue(AccountStatus(R.string.InvalidCredentials, false))
                    inputError = true
                } catch (e: FirebaseAuthInvalidUserException) {
                    actionResult.postValue(AccountStatus(R.string.InvalidCredentials, false))
                    inputError = true
                } catch (e: Exception) {
                    auth.signOut()
                    Log.e(AUTH_TAG, e.stackTraceToString())
                    actionResult.postValue(AccountStatus(R.string.SystemError, false))
                }
            }
        } else {
            actionResult.postValue(AccountStatus(R.string.FillRequiredFields, false))
            inputError = true
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}