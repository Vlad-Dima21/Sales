package vlad.dima.sales.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import vlad.dima.sales.room.user.User
import vlad.dima.sales.room.user.UserDao

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): LiveData<List<User>> = userDao.getAll()

    fun getUserByUID(uid: String): LiveData<User> = userDao.getWithUID(uid)

    suspend fun upsertUser(user: User) = userDao.insert(user)
}