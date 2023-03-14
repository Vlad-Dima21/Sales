package vlad.dima.sales.ui.dashboard.common.notifications

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    var title: String = "",
    var description: String = "",
    var managerUID: String = "",
    var importance: Int = 0,
    var createdDate: Date = Date()
)