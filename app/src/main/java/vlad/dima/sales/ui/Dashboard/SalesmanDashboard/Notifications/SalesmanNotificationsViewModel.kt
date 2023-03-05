package vlad.dima.sales.ui.Dashboard.SalesmanDashboard.Notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SalesmanNotificationsViewModel: ViewModel() {

    val isUserLoggedIn = MutableLiveData<Boolean>(true)
    val sampleData = (1..100).map { it }

    fun Logout() {
        FirebaseAuth.getInstance().signOut()
        isUserLoggedIn.postValue(false)
    }
}