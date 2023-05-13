package vlad.dima.sales.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.order.OrderDao
import vlad.dima.sales.room.order.OrderProduct
import vlad.dima.sales.room.order.OrderProductDao

class OrderRepository(private val orderDao: OrderDao, private val orderProductDao: OrderProductDao) {

    fun getAllOrders(): Flow<List<Order>> = orderDao.getAll()

    fun getOrderById(id: Int): Flow<Order> = orderDao.getById(id)

    fun getOrdersBySalesmanUID(uid: String): Flow<List<Order>> = orderDao.getBySalesmanUID(uid)

    fun upsertOrder(order: Order): List<Long> = orderDao.insert(order)

    fun deleteOrder(order: Order) = orderDao.delete(order)

    fun getOrderProductsBySalesmanUID(uid: String) = orderProductDao.getBySalesmanUID(uid)

    fun getOrderProductsByOrderId(orderId: Int) = orderProductDao.getByOrderId(orderId)

    fun upsertOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.insert(orderProducts)

    fun deleteOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.delete(orderProducts)
}