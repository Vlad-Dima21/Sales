package vlad.dima.sales.model

import java.util.*

data class NotificationMessage(
    var notificationId: String = "",
    var authorUID: String = "",
    var authorName: String = "",
    var message: String = "",
    var sendDate: Date = Date()
)
