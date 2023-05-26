package vlad.dima.sales.model

import com.google.firebase.firestore.Exclude
data class Client(
    @Exclude var clientId: String = "",
    var clientName: String = "",
    var address: String = "",
    var contactName: String = "",
    var contactPhone: String = "",
    var managerUID: String = "",
): java.io.Serializable
