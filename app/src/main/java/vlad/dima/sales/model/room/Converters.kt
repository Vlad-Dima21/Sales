package vlad.dima.sales.model.room

import androidx.room.TypeConverter
import java.util.Date

public class Converters {
    @TypeConverter
    public fun fromDateToLong(date: Date): Long = date.time
    @TypeConverter
    public fun fromLongToDate(dateInMillis: Long): Date = Date(dateInMillis)
}