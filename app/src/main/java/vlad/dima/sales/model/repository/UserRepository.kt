package vlad.dima.sales.model.repository

import kotlinx.coroutines.flow.Flow
import vlad.dima.sales.model.User
import vlad.dima.sales.model.room.user.UserDao

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> = userDao.getAll()

    fun getUserByUID(uid: String): Flow<User> = userDao.getByUID(uid)

    fun upsertUser(user: User) = userDao.insert(user)
}