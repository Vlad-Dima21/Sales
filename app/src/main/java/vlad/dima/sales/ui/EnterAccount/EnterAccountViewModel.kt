package vlad.dima.sales.ui.EnterAccount

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vlad.dima.sales.R

class EnterAccountViewModel: ViewModel() {
    var emailFieldState by mutableStateOf("")
    var usernameFieldState by mutableStateOf("")
    var passwordFieldState by mutableStateOf("")
    var inputError by mutableStateOf(false)

    val actionResult: MutableLiveData<AccountStatus> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    fun signUpUser() {
        inputError = false

        if (emailFieldState.isNotEmpty() && usernameFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    auth.createUserWithEmailAndPassword(emailFieldState, passwordFieldState).await()
                    auth.currentUser?.let { user ->
                        val addUsernameToAccount = UserProfileChangeRequest.Builder()
                            .setDisplayName(usernameFieldState)
                            .build()
                        user.updateProfile(addUsernameToAccount)
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
                    Log.d("SERVER_RESPONSE", e.stackTraceToString())
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
                    actionResult.postValue(
                        when {
                            isLoggedIn() -> AccountStatus(R.string.LogInSuccessful, true)
                            else -> AccountStatus(R.string.LogInUnsuccessfull, false)
                        }
                    )
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    actionResult.postValue(AccountStatus(R.string.InvalidCredentials, false))
                    inputError = true
                } catch (e: Exception) {
                    Log.d("SERVER_RESPONSE", e.stackTraceToString())
                }
            }
        } else {
            actionResult.postValue(AccountStatus(R.string.FillRequiredFields, false))
            inputError = true
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}