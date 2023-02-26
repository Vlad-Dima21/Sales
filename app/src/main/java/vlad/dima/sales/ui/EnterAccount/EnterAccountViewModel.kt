package vlad.dima.sales.ui.EnterAccount

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EnterAccountViewModel: ViewModel() {
    var emailFieldState by mutableStateOf("")
    var passwordFieldState by mutableStateOf("")
    private var validCredentials: Boolean = false
        get() = emailFieldState.isNotEmpty() && passwordFieldState.isNotEmpty()

    val toastMessageObserver: MutableLiveData<String> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    fun signUpUser() {
        if (validCredentials) {
            viewModelScope.launch {
                try {
                    auth.createUserWithEmailAndPassword(emailFieldState, passwordFieldState).await()
                        toastMessageObserver.postValue(when {
                            isLoggedIn() -> "User signed up"
                            else -> "User didn't sign up"
                        })
                    }catch (e: Exception) {
                        toastMessageObserver.postValue(e.message)
                }
            }
        } else {
            toastMessageObserver.postValue("Please fill required fields")
        }
    }

    private fun isLoggedIn(): Boolean = auth.currentUser != null
}