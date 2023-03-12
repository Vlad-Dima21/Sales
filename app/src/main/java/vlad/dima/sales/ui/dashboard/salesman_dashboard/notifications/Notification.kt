package vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications

data class Notification(
    var title: String = "",
    var description: String = "",
    var managerUID: String = "",
    var importance: Int = 0
)
