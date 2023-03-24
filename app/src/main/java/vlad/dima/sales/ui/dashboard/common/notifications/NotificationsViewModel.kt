package vlad.dima.sales.ui.dashboard.common.notifications

import android.content.Intent
import kotlinx.coroutines.Job

interface NotificationsViewModel {
    fun loadItems(): Job
    fun viewNotification(intent: Intent): Job
}