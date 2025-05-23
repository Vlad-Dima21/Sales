package vlad.dima.sales.model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var managerUID: String = "",
    var importance: Int = 0,
    var createdDate: Date = Date()
)