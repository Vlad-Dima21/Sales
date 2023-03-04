package vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SalesmanNotificationsViewModel: ViewModel() {

    val isUserLoggedIn = MutableLiveData<Boolean>(true)

    fun Logout() {
        FirebaseAuth.getInstance().signOut()
        isUserLoggedIn.postValue(false)
    }
}