package vlad.dima.sales.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import vlad.dima.sales.room.user.User
import vlad.dima.sales.room.user.UserDao

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> = userDao.getAll()

    fun getUserByUID(uid: String): Flow<User> = userDao.getByUID(uid)

    fun upsertUser(user: User) = userDao.insert(user)
}