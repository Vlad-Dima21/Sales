package vlad.dima.sales.model.room.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vlad.dima.sales.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE userUID = :uid")
    fun getByUID(uid: String): Flow<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: User)

    @Delete
    fun delete(vararg users: User)
}