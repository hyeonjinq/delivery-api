package com.fastcampus.deliveryapi.repository.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface OrderRepository: JpaRepository<Order, Long> {

    fun existsByCheckoutId(checkoutId: Long): Boolean

    fun findByOrderIdAndCustomerId(orderId: Long, customerId: Long): Optional<Order>

}