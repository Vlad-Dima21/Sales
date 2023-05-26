package vlad.dima.sales.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    var fullName: String = "",
    var managerUID: String = "unassigned",
    @PrimaryKey var userUID: String = ""
)
