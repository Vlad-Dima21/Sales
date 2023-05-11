package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

data class Client(
    var clientId: String = "",
    var clientName: String = "",
    var address: String = "",
    var contactName: String = "",
    var contactPhone: String = "",
    var managerUID: String = "",
): java.io.Serializable
