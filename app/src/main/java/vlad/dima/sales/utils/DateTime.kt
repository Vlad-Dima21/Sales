package vlad.dima.sales.utils

import java.util.Calendar
import java.util.Date

class DateTime(
    val date: Date
) {

    override fun equals(other: Any?): Boolean {
        if (other === this)
            return true
        else if (other !is DateTime)
            return false
        return sortableDate(this.date) == sortableDate(other.date)
    }

    override fun hashCode(): Int {
        return sortableDate(this.date).hashCode()
    }

    companion object {
        fun getDate(date: Date): String {
            val cl = Calendar.getInstance().apply { time = date }
            return "${cl[Calendar.DAY_OF_MONTH].toString().padStart(2, '0')}.${(cl[Calendar.MONTH] + 1).toString().padStart(2, '0')}.${cl[Calendar.YEAR]}"
        }

        private fun sortableDate(date: Date): String {
            val cl = Calendar.getInstance().apply { time = date }
            return "${cl[Calendar.YEAR]}.${(cl[Calendar.MONTH] + 1).toString().padStart(2, '0')}.${cl[Calendar.DAY_OF_MONTH].toString().padStart(2, '0')}"
        }

        fun getTime(date: Date): String {
            val cl = Calendar.getInstance().apply { time = date }
            return "${cl[Calendar.HOUR_OF_DAY].toString().padStart(2, '0')}:${cl[Calendar.MINUTE].toString().padStart(2, '0')}"
        }
    }
}