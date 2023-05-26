package vlad.dima.sales.model.repository

import kotlinx.coroutines.flow.Flow
import vlad.dima.sales.model.Order
import vlad.dima.sales.model.room.order.OrderDao
import vlad.dima.sales.model.OrderProduct
import vlad.dima.sales.model.room.order.OrderProductDao

class OrderRepository(private val orderDao: OrderDao, private val orderProductDao: OrderProductDao) {

    fun getAllOrders(): Flow<List<Order>> = orderDao.getAll()

    fun getOrderById(id: Int): Flow<Order> = orderDao.getById(id)

    fun getOrdersBySalesmanUID(uid: String): Flow<List<Order>> = orderDao.getBySalesmanUID(uid)

    suspend fun upsertOrder(order: Order): List<Long> = orderDao.insert(order)

    suspend fun deleteOrders(vararg orders: Order) = orderDao.delete(*orders)

    fun getOrderProductsBySalesmanUID(uid: String) = orderProductDao.getBySalesmanUID(uid)

    fun getOrderProductsByOrderId(orderId: Int) = orderProductDao.getByOrderId(orderId)

    suspend fun upsertOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.insert(orderProducts)

    suspend fun deleteOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.delete(orderProducts)
}