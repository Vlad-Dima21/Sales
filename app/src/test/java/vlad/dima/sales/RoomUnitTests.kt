package vlad.dima.sales

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.OrderProduct
import vlad.dima.sales.model.User
import vlad.dima.sales.model.repository.OrderRepository
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class RoomUnitTests {
    private lateinit var database: SalesDatabase
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository

    @Before
    fun createTestDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SalesDatabase::class.java).allowMainThreadQueries().build()

        val userDao = database.userDao()
        userRepository = UserRepository(userDao)
        orderRepository = database.let {
            OrderRepository(it.orderDao(), it.orderProductDao())
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun check_UserUpsert() = runTest {
        val user1 = User(
            fullName = "John",
            email = "john@email.com",
            userUID = "john_id"
        )
        val user2 = User(
            fullName = "Karen",
            email = "karen@email.com",
            userUID = "karen_id"
        )
        val user3 = User(
            fullName = "Phillip",
            email = "phillip@email.com",
            userUID = "john_id"             // same id as John
        )

        userRepository.upsertUser(user1)
        userRepository.upsertUser(user2)
        userRepository.upsertUser(user3)

        userRepository.getAllUsers().test {
            assert(awaitItem().size == 2)
        }
    }

    @Test
    fun check_UserGetByUID() = runTest {
        val user1 = User(
            fullName = "John",
            email = "john@email.com",
            userUID = "john_id"
        )
        val user2 = User(
            fullName = "Phillip",
            email = "phillip@email.com",
            userUID = "john_id"             // same id as John
        )

        userRepository.upsertUser(user1)
        userRepository.upsertUser(user2)

        userRepository.getUserByUID("john_id").test {
            assert(awaitItem().fullName == "Phillip")
        }
    }

    @Test
    fun check_OrderProductUpsert() = runTest {
        val salesman = User(
            fullName = "John",
            email = "john@email.com",
            userUID = "john_id"
        )
        val newOrder = Order(
            clientId = "client",
            salesmanUID = salesman.userUID,
            total = 10f
        )
        userRepository.upsertUser(salesman)
        val orderId = orderRepository.upsertOrder(newOrder)[0]
        orderRepository.upsertOrderProducts(
            listOf(
                OrderProduct(orderId.toInt(), "product1", 1),
                OrderProduct(orderId.toInt(), "product2", 3)
            )
        )
        orderRepository.getOrderProductsByOrderId(orderId.toInt()).test {
            assert(awaitItem().size == 2)
        }
    }

    @Test
    fun check_OrderProductDeletion() = runTest {
        val salesman = User(
            fullName = "John",
            email = "john@email.com",
            userUID = "john_id"
        )
        val newOrder = Order(
            clientId = "client",
            salesmanUID = salesman.userUID,
            total = 10f
        )
        userRepository.upsertUser(salesman)
        val orderId = orderRepository.upsertOrder(newOrder)[0]
        orderRepository.upsertOrderProducts(
            (0 until 100).map {
                OrderProduct(orderId.toInt(), "product$it", it * 2 + 1)
            }
        )
        orderRepository.getOrderProductsByOrderId(orderId.toInt()).test {
            assert(awaitItem().size == 100)
        }
        orderRepository.deleteOrders(newOrder.copy(orderId = orderId.toInt()))
        orderRepository.getOrderProductsByOrderId(orderId.toInt()).test {
            assert(awaitItem().isEmpty())
        }
    }
}