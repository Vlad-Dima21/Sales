package vlad.dima.sales.repository

import androidx.lifecycle.LiveData
import vlad.dima.sales.room.order.Order
import vlad.dima.sales.room.order.OrderDao
import vlad.dima.sales.room.order.OrderProduct
import vlad.dima.sales.room.order.OrderProductDao

class OrderRepository(private val orderDao: OrderDao, private val orderProductDao: OrderProductDao) {

    fun getAllOrders(): LiveData<List<Order>> = orderDao.getAll()

    fun getOrderById(id: Int): LiveData<Order> = orderDao.getById(id)

    fun getOrdersBySalesmanUID(uid: String): LiveData<List<Order>> = orderDao.getBySalesmanUID(uid)

    fun upsertOrder(order: Order) = orderDao.insert(order)

    fun deleteOrder(order: Order) = orderDao.delete(order)

    fun getOrderProductsBySalesmanUID(uid: String) = orderProductDao.getBySalesmanUID(uid)

    fun upsertOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.insert(orderProducts)

    fun deleteOrderProducts(orderProducts: List<OrderProduct>) = orderProductDao.delete(orderProducts)
}