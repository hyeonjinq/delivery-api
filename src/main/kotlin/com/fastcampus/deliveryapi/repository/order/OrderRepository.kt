package com.fastcampus.deliveryapi.repository.order

import com.fastcampus.deliveryapi.domain.order.OrderStatus
import com.fastcampus.deliveryapi.domain.order.OrderStore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface OrderRepository: JpaRepository<Order, Long> {

    fun existsByCheckoutId(checkoutId: Long): Boolean

    fun findByOrderIdAndCustomerId(orderId: Long, customerId: Long): Optional<Order>

    @Query(
        value = """
            SELECT new com.fastcampus.deliveryapi.domain.order.OrderStore(o.orderId, o.orderStatus, o.totalAmount, o.discountAmount, s.storeId, s.storeName)
            FROM Order as o
            JOIN Store as s ON o.storeId = s.storeId
            WHERE o.customerId = :customerId and o.orderStatus = :orderStatus AND o.isDeleted = false
        """
    )
    fun findAllByCustomerId(@Param("customerId") customerId: Long, @Param("orderStatus") orderStatus: OrderStatus): List<OrderStore>

}