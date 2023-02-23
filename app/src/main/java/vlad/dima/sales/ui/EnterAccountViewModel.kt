package vlad.dima.sales.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EnterAccountViewModel: ViewModel() {
    var emailFieldState by mutableStateOf("")
    var passwordFieldState by mutableStateOf("")
}