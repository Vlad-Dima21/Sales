package vlad.dima.sales.room.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    var fullName: String = "",
    var managerUID: String = "",
    @PrimaryKey var userUID: String = ""
)
